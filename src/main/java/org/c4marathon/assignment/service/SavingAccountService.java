package org.c4marathon.assignment.service;


import org.c4marathon.assignment.common.exception.BalanceUpdateException;
import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.c4marathon.assignment.common.util.BankSystemUtil;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.SavingAccount;
import org.c4marathon.assignment.dto.request.ChargeSavingAccountRequestDto;
import org.c4marathon.assignment.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountService {
	private final SavingAccountRepository savingAccountRepository;
	private final MainAccountService mainAccountService;
	static int counter = 50;

	@Transactional
	public void createSavingAccount(SavingAccountRequestDto requestDto) {
		MainAccount mainAccount = mainAccountService.getMainAccount(requestDto.mainAccountId());
		SavingAccount savingAccount = new SavingAccount(mainAccount, BankSystemUtil.createAccountNumber(counter), requestDto.balance(), requestDto.rate());
		savingAccountRepository.save(savingAccount);
	}

	/**
	 * 메인 계좌 => 적금 계좌로 돈 충전
	 * [1] 메인 계좌 => 적금 계좌 방향으로 돈이 흐르니, MainAccount 먼저 Lock 획득
	 * [2] 잔고 파악
	 * [3] 적금 계좌 업데이트
	 * [4] 메인 계좌 업데이트
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void chargeFromMainAccount(ChargeSavingAccountRequestDto requestDto){
		MainAccount mainAccount = mainAccountService.getMainAccountWithXLock(requestDto.mainAccountId());

		//잔고 파악
		if(!mainAccount.checkBalanceAvailability(requestDto.money())){
			throw new BalanceUpdateException(ErrorCode.BALANCE_NOT_ENOUGH);
		}

		SavingAccount savingAccount = getSavingAccountWithXLock(requestDto.savingAccountId());
		savingAccount.chargeMoney(requestDto.money());
		savingAccountRepository.save(savingAccount);
		mainAccountService.withdrawMoney(mainAccount, requestDto.money());
	}

	private SavingAccount getSavingAccountWithXLock(long savingAccountId){
		return savingAccountRepository.findByIdWithXLock(savingAccountId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_SAVING_ACCOUNT));
	}
}
