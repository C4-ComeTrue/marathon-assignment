package org.c4marathon.assignment.application;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.response.CreatedAccountInfo;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.dto.response.WithdrawResult;
import org.c4marathon.assignment.global.AccountUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	// 단순 출금
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public WithdrawResult withdraw(String accountNumber, long money) {
		LocalDateTime withdrawTime = LocalDateTime.now();

		updateWithdrawLimit(accountNumber, money, withdrawTime);
		updateBalance(accountNumber, -money);

		return new WithdrawResult(money);
	}

	// 거래
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TransferResult transfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		LocalDateTime transferTime = LocalDateTime.now();

		validateSender(senderAccountNumber, receiverAccountNumber);
		updateWithdrawLimit(senderAccountNumber, money, transferTime);

		transferWithoutDeadlock(senderAccountNumber, receiverAccountNumber, money);

		return new TransferResult(senderAccountNumber, receiverAccountNumber, money);
	}

	/**
	 *
	 * @param userId
	 * @param accountType
	 * @return
	 *
	 * 계좌 생성. accountType은 생성 계좌가 적금 계좌인지 입출금 계좌인지를 판단.
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public CreatedAccountInfo create(long userId, AccountType accountType) {
		userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));

		String accountNumber = AccountUtils.getAccountNumber();

		Account savedAccount = accountRepository.save(
			Account.builder().userId(userId).accountType(accountType).accountNumber(accountNumber).build());

		return new CreatedAccountInfo(savedAccount.getAccountNumber(), savedAccount.getCreatedAt(),
			savedAccount.getAccountType(), savedAccount.getBalance(), savedAccount.isMain());
	}

	private void updateBalance(String accountNumber, long money) {
		int updatedRow = accountRepository.addBalance(money, accountNumber);

		if (updatedRow == 0)
			throw new RuntimeException("Failed to update balance.");
	}

	/**
	 * @param senderAccountNumber
	 * @param receiverAccountNumber
	 *
	 * 적금 계좌는 송신 계좌가 메인 계좌여야 하기 때문에 그 조건을 판단하는 역할을 하는 메서드.
	 */
	private void validateSender(String senderAccountNumber, String receiverAccountNumber) {
		Account receiver = accountRepository.findByAccountNumber(receiverAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found."));

		if (receiver.getAccountType() != AccountType.INSTALLATION)
			return;

		Account mainAccount = accountRepository.findMainAccount(receiver.getUserId())
			.orElseThrow(() -> new RuntimeException("Account not found."));

		if (!mainAccount.getAccountNumber().equals(senderAccountNumber))
			throw new RuntimeException("적금 계좌는 본인의 메인 계좌에서만 거래 가능합니다.");
	}

	// 한도 확인
	private void updateWithdrawLimit(String accountNumber, long money, LocalDateTime withdrawTime) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found."));
		User user = userRepository.findById(account.getUserId())
			.orElseThrow(() -> new RuntimeException("User not found."));

		LocalDateTime lastWithdrawDate = user.getLastWithdrawDate();

		if (lastWithdrawDate.toLocalDate().isBefore(withdrawTime.toLocalDate())) {
			userRepository.initializeWithdrawLimit(user.getId());
		}

		userRepository.withdraw(user.getId(), money, withdrawTime);
	}

	// 순환 대기 문제 해결을 위해 계좌 번호 사전 순으로 트랜잭션 처리
	private void transferWithoutDeadlock(String senderAccountNumber, String receiverAccountNumber, long money) {
		if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
			updateBalance(senderAccountNumber, -money);
			updateBalance(receiverAccountNumber, money);
		}
		else {
			updateBalance(receiverAccountNumber, money);
			updateBalance(senderAccountNumber, -money);
		}
	}
}
