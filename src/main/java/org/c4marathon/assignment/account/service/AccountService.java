package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.global.util.Const.*;
import static org.c4marathon.assignment.transactional.domain.TransactionStatus.PENDING_DEPOSIT;
import static org.c4marathon.assignment.transactional.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transactional.domain.TransactionType.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.c4marathon.assignment.transactional.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transactional.exception.NotFoundTransactionException;
import org.c4marathon.assignment.transactional.exception.UnauthorizedTransactionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final MemberRepository memberRepository;
	private final TransactionalRepository transactionalRepository;
	private final SavingAccountRepository savingAccountRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void createAccount(Long memberId) {
		Account account = Account.create(DEFAULT_BALANCE);
		accountRepository.save(account);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(NotFoundMemberException::new);
		member.setMainAccountId(account.getId());

		memberRepository.save(member);
	}

	/**
	 * 메인 계좌에 돈을 충전하다.
	 * 한 번에 메인 계좌에다가 충전을 여러 번 할 수도 있다. 어떻게 관리해야하나?
	 * @param accountId
	 * @param money
	 */
	//기본값 -> Repeatable Read
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void chargeMoney(Long accountId, long money) {
		Account account = accountRepository.findByIdWithLock(accountId)
			.orElseThrow(NotFoundAccountException::new);

		if (!account.isChargeWithinDailyLimit(money)) {
			throw new DailyChargeLimitExceededException();
		}

		account.deposit(money);
		accountRepository.save(account);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToSavingAccount(Long accountId, Long savingAccountId, long money) {
		Account account = accountRepository.findByIdWithLock(accountId)
			.orElseThrow(NotFoundAccountException::new);

		if (!account.isSend(money)) {
			autoCharge(money, account);
		}

		SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
			.orElseThrow(NotFoundAccountException::new);

		account.withdraw(money);
		accountRepository.save(account);

		savingAccount.deposit(money);
		savingAccountRepository.save(savingAccount);
	}

	/**
	 * 송금 시 송금 내역을 저장하는 이벤트 발행 후 커밋
	 * @param senderAccountId
	 * @param request
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void withdraw(Long senderAccountId, WithdrawRequest request) {
		Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
			.orElseThrow(NotFoundAccountException::new);

		if (!senderAccount.isSend(request.money())) {
			autoCharge(request.money(), senderAccount);
		}

		senderAccount.withdraw(request.money());
		accountRepository.save(senderAccount);

		// String transactionId = UUID.randomUUID().toString();

		if (request.type().equals(IMMEDIATE_TRANSFER)) {
			eventPublisher.publishEvent(
				new TransactionCreateEvent(
					senderAccountId,
					request.receiverAccountId(),
					request.money(),
					request.type(),
					WITHDRAW,
					LocalDateTime.now()
				)
			);
		} else if (request.type().equals(PENDING_TRANSFER)) {
			eventPublisher.publishEvent(
				new TransactionCreateEvent(
					senderAccountId,
					request.receiverAccountId(),
					request.money(),
					request.type(),
					PENDING_DEPOSIT,
					LocalDateTime.now()
				)
			);
		}
	}

	/**
	 * 송금 취소 기능
	 * 취소하려는 송금 내역을 가져와 검증 후 송금을 취소함
	 * @param senderAccountId
	 * @param transactionalId
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void cancelWithdraw(Long senderAccountId, Long transactionalId) {
		Transaction transactional = transactionalRepository.findTransactionalByTransactionalIdWithLock(transactionalId)
			.orElseThrow(NotFoundTransactionException::new);

		validationTransactional(senderAccountId, transactional);

		Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
			.orElseThrow(NotFoundAccountException::new);

		senderAccount.deposit(transactional.getAmount());
		transactional.updateStatus(CANCEL);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void rollbackWithdraw(Long senderAccountId, long money) {
		Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
			.orElseThrow(NotFoundAccountException::new);

		senderAccount.deposit(money);
		accountRepository.save(senderAccount);
	}

	/**
	 * 송금할 때 메인 계좌에 잔액이 부족할 때 10,000원 단위로 충전하는 로직
	 * @param money
	 * @param senderAccount
	 */
	private void autoCharge(long money, Account senderAccount) {

		long needMoney = money - senderAccount.getMoney();
		long chargeMoney = ((needMoney + CHARGE_AMOUNT - 1) / CHARGE_AMOUNT) * CHARGE_AMOUNT;

		if (!senderAccount.isChargeWithinDailyLimit(chargeMoney)) {
			throw new DailyChargeLimitExceededException();
		}

		senderAccount.deposit(chargeMoney);
	}


	private static void validationTransactional(Long senderAccountId, Transaction transactional) {
		if (!transactional.getSenderAccountId().equals(senderAccountId)) {
			throw new UnauthorizedTransactionException();
		}

		if (!transactional.getStatus().equals(PENDING_DEPOSIT)) {
			throw new InvalidTransactionStatusException();
		}
	}

}
