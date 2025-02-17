package org.c4marathon.assignment.service;

import java.time.LocalDateTime;

import org.c4marathon.assignment.common.exception.BalanceUpdateException;
import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.c4marathon.assignment.common.util.BankSystemUtil;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.dto.request.ChargeMainAccountRequestDto;
import org.c4marathon.assignment.dto.request.TransferRequestDto;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountService {
	private final MainAccountRepository mainAccountRepository;
	private final RedisTemplate<String,Long> redisTemplate;
	static final long CHARGE_LIMIT = 3000000L;
	static int counter = 1;

	@Transactional
	public void createMainAccount(User user){
		MainAccount mainAccount = new MainAccount(user, BankSystemUtil.createAccountNumber(counter), 0, CHARGE_LIMIT,
			LocalDateTime.now());
		mainAccountRepository.save(mainAccount);
	}

	/** [Step1] Redis에 Daily Limit 롤백
	 * */
	public void rollbackDailyChargeLimit(long mainAccountId, long money){
		String key = "dailyLimit:" + mainAccountId;
		Long currentLimit = redisTemplate.opsForValue().get(key);
		if (currentLimit != null) {
			redisTemplate.opsForValue().set(key, currentLimit + money);
		}
	}

	/**
	 * [Step1] 메인 계좌 충전 Method
	 * [1] Redis를 통해 일일 충전 한도가 넘었는지 확인
	 * [2] balance 업데이트 성공 했을 경우에 DB에도 저장
	 * [3] balance 업데이트 실패했을때 rollback
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void chargeMainAccount(ChargeMainAccountRequestDto requestDto){

		/* mainAccountId가 존재하는지 여부 파악 (existId는 Lock을 걸지 않고 조회한다.) */
		if(!mainAccountRepository.existsById(requestDto.mainAccountId())) {
			throw new NotFoundException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT);
		}

		try {
			if(!validateDailyChargeLimit(requestDto.mainAccountId(), requestDto.money())){
				throw new IllegalStateException(ErrorCode.DAILY_LIMIT_EXCEEDED.getMessage());
			}
			updateMainAccountBalance(requestDto.mainAccountId(), requestDto.money());
		} catch (Exception e){
			if(e instanceof BalanceUpdateException){
				rollbackDailyChargeLimit(requestDto.mainAccountId(), requestDto.money());
			}
			throw e;
		}

	}

	/**
	 * [Step2] 친구 계좌로 송금
	 * 보내는 사람의 계좌에서 돈을 차감하고, 스케줄러를 통해서 돈을 추가해주기
	 * 트랜잭션 범위를 최대한 분리
	 * **/
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void transferToOtherAccount(TransferRequestDto requestDto) {

		MainAccount sender = getMainAccountWithXLock(requestDto.senderAccountId());

		long chargeAmount = checkBalanceAvailability(sender, requestDto.amount());
		sender.withdrawMoney(requestDto.amount());
		sender.updateChargeDate();
		try {
			mainAccountRepository.save(sender);
			String key = "transfer:" + requestDto.senderAccountId() + ":" + requestDto.receiverAccountId() + ":" + chargeAmount;
			redisTemplate.opsForValue().set(key, requestDto.amount());
		} catch (Exception e){
			rollbackDailyChargeLimit(requestDto.senderAccountId(),chargeAmount);
		}
	}

	public MainAccount getMainAccount(long mainAccountId){
		return mainAccountRepository.findById(mainAccountId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT));
	}

	public MainAccount getMainAccountWithXLock(long mainAccountId){
		return mainAccountRepository.findByIdWithXLock(mainAccountId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT));
	}

	public void withdrawMoney(MainAccount mainAccount, long money){
		mainAccount.withdrawMoney(money);
		mainAccountRepository.save(mainAccount);
	}

	/**
	 * [Step2] 송금시 잔액 부족 파악후 10,000단위로 충전
	 * **/
	private long checkBalanceAvailability(MainAccount mainAccount, long amount){
		//송금 가능 여부 파악(잔고)
		long chargeAmount = 0;
		if (!mainAccount.checkBalanceAvailability(amount)) {

			long needAmount = amount - mainAccount.getBalance();
			if (needAmount > 0) { // 충전이 필요한 경우
				chargeAmount = ((needAmount + 9999) / 10000) * 10000; //10,000원 단위로 올림

				if (!validateDailyChargeLimit(mainAccount.getId(), chargeAmount)) { //redis에 일일 한도 확인
					throw new IllegalStateException(ErrorCode.DAILY_LIMIT_EXCEEDED.getMessage());
				}
				mainAccount.chargeMoney(chargeAmount);
			}
		}
		return chargeAmount;
	}

	/** [Step1] 일일 한도 체크
	 * Redis Cache를 통해 일일 충전한도에 맞는지 여부 확인
	 * */
	private boolean validateDailyChargeLimit(long mainAccountId, long money){

		/* 24시에 10분동안 은행 계좌 점검 => 10분동안 일일 한도 초기화*/
		String lockKey = "reset";
		if (redisTemplate.hasKey(lockKey)) {
			throw new IllegalStateException(ErrorCode.REDIS_RESET_EXCEPTION.getMessage());
		}

		/* Redis에 해당 계좌id가 없다면 일일한도 3,000,000으로 세팅 */
		Long currentLimit = redisTemplate.opsForValue().get("dailyLimit:"+mainAccountId);
		if(currentLimit == null) {
			currentLimit = CHARGE_LIMIT;
			redisTemplate.opsForValue().set("dailyLimit:" + mainAccountId,currentLimit);
		}

		/* 일일 한도를 넘었을 경우 */
		if(currentLimit < money) return false;

		/* 일일 한도를 넘지 않은 경우  */
		redisTemplate.opsForValue().set("dailyLimit:" + mainAccountId, currentLimit-money);
		return true;
	}

	/** [Step1] Main 계좌 잔고 수정
	 * balance를 update하기 위해서 Lock 을 걸고 조회 수행
	 * limit에도 -money 만큼 한도를 뺼수 있도록 조정
	 * 트랜잭션 시간을 지금으로 설정
	 * */
	private void updateMainAccountBalance(long mainAccountId, long money){
		try {
			MainAccount mainAccount = getMainAccountWithXLock(mainAccountId);
			mainAccount.chargeMoney(money);
			mainAccount.updateChargeDate();
			mainAccountRepository.save(mainAccount);
		} catch (Exception e){
			throw new BalanceUpdateException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT);
		}

	}

}
