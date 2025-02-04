package org.c4marathon.assignment.service;

import java.util.Random;

import org.c4marathon.assignment.common.exception.BalanceUpdateException;
import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.c4marathon.assignment.common.util.BankSystemUtil;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.dto.request.ChargeMainAccountRequestDto;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountService {
	private final MainAccountRepository mainAccountRepository;
	private final RedisTemplate<String,Long> redisTemplate;
	static final long chargeLimit = 3000000L;
	static int counter = 1;

	@Transactional
	public void createMainAccount(User user){
		MainAccount mainAccount = new MainAccount(user, BankSystemUtil.createAccountNumber(counter), 0);
		mainAccountRepository.save(mainAccount);
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
	 * 메인 계좌 충전 Method
	 * [1] Redis를 통해 일일 충전 한도가 넘었는지 확인
	 * [2] balance 업데이트
	 * [3] balance 업데이트 실패했을때 rollback
	 * */
	@Transactional
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

	/** [1]
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
			currentLimit = chargeLimit;
			redisTemplate.opsForValue().set("dailyLimit:" + mainAccountId,currentLimit);
		}

		/* 일일 한도를 넘었을 경우 */
		if(currentLimit < money) return false;

		/* 일일 한도를 넘지 않은 경우  */
		redisTemplate.opsForValue().set("dailyLimit:" + mainAccountId, currentLimit-money);
		return true;
	}

	/** [2]
	 * balance를 update하기 위해서 Lock 을 걸고 조회 수행
	 * */
	private void updateMainAccountBalance(long mainAccountId, long money){
		try {
			MainAccount mainAccount = getMainAccountWithXLock(mainAccountId);
			mainAccount.chargeMoney(money);
			mainAccountRepository.save(mainAccount);
		} catch (Exception e){
			throw new BalanceUpdateException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT);
		}

	}

	/** [3]
	 * balance를 update 실패 했을 때 rollback
	 * */
	private void rollbackDailyChargeLimit(long mainAccountId, long money){
		String key = "dailyLimit:" + mainAccountId;
		Long currentLimit = redisTemplate.opsForValue().get(key);
		if (currentLimit != null) {
			redisTemplate.opsForValue().set(key, currentLimit + money);
		}
	}

}
