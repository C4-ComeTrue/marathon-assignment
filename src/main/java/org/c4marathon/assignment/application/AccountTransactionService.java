package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountTransactionService {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateBalance(String accountNumber, long money) {
		if (money > 0) {
			updateUser(accountNumber, money);
		}

		updateAccount(accountNumber, money);
	}

	// 순환 대기 문제 해결을 위해 계좌 번호 사전 순으로 트랜잭션 처리
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
			updateBalance(senderAccountNumber, -money);
			updateBalance(receiverAccountNumber, money);
		}
		else {
			updateBalance(receiverAccountNumber, money);
			updateBalance(senderAccountNumber, -money);
		}
	}

	private void updateUser(String accountNumber, long money) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found"));

		int updatedRow = userRepository.charge(account.getUserId(), money);

		if (updatedRow == 0)
			throw new RuntimeException("Account not charged");
	}

	private void updateAccount(String accountNumber, long money) {
		int updatedRow = accountRepository.updateBalance(accountNumber, money);

		if (updatedRow == 0)
			throw new RuntimeException("Failed to update balance.");
	}
}
