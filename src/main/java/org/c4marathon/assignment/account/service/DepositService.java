package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transaction.exception.NotFoundTransactionException;
import org.c4marathon.assignment.transaction.exception.UnauthorizedTransactionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	/**
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = WITHDRAW인 송금 내역을 조회해서 입금 처리를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 실패 : AOP를 통해 송금 내역 status를 FAILED_DEPOSIT로 변경한다.
	 * @param transactional
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void successDeposit(Transaction transactional) {
		processDeposit(transactional);
	}

	/**
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = FAILED_DEPOSIT인 송금 내역을 조회해서 입금 재시도를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 재시도 실패 : AOP를 통해 송금 내역 status를 CANCEL로 변경 후 송금 취소한다.
	 * @param transactional
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void failedDeposit(Transaction transactional) {
		processDeposit(transactional);
	}

	/**
	 * 금액을 받는 사용자가 직접 확인 후 금액을 받는 비즈니스 로직
	 * @param receiverAccountId
	 * @param transactionalId
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void depositByReceiver(Long receiverAccountId, Long transactionalId) {
		Transaction transaction = transactionRepository.findTransactionalByTransactionIdWithLock(transactionalId)
			.orElseThrow(NotFoundTransactionException::new);

		validationTransaction(receiverAccountId, transaction);

		Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
			.orElseThrow(NotFoundAccountException::new);

		receiverAccount.deposit(transaction.getAmount());
		transaction.updateStatus(SUCCESS_DEPOSIT);
	}

	private void processDeposit(Transaction transactional) {
		Long receiverAccountId = transactional.getReceiverAccountId();
		long amount = transactional.getAmount();

		Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
			.orElseThrow(NotFoundAccountException::new);

		receiverAccount.deposit(amount);
		accountRepository.save(receiverAccount);

		transactional.setReceiverTime(LocalDateTime.now());
		transactional.updateStatus(SUCCESS_DEPOSIT);
		transactionRepository.save(transactional);

	}

	private static void validationTransaction(Long receiverAccountId, Transaction transaction) {
		if (!transaction.getReceiverAccountId().equals(receiverAccountId)) {
			throw new UnauthorizedTransactionException();
		}

		if (!transaction.getStatus().equals(PENDING_DEPOSIT)) {
			throw new InvalidTransactionStatusException();
		}
	}
}
