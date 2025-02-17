package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LazyWireTransferStrategy implements WireTransferStrategy {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		Transaction transaction = Transaction.builder()
			.senderAccountNumber(senderAccountNumber)
			.receiverAccountNumber(receiverAccountNumber)
			.balance(money)
			.state(TransactionState.PENDING)
			.build();

		transactionRepository.save(transaction);
		updateBalance(senderAccountNumber, -money);
	}

	private void updateBalance(String senderAccountNumber, long money) {
		int updatedRow = accountRepository.updateBalance(senderAccountNumber, money);

		if (updatedRow == 0)
			throw new RuntimeException("Failed to update balance");
	}
}
