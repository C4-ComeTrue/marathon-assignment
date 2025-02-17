package org.c4marathon.assignment.transaction.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.mail.NotificationService;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest extends IntegrationTestSupport {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private AccountRepository accountRepository;


	@MockBean
	private NotificationService notificationService;

	@AfterEach
	void tearDown() {
		transactionRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();

	}

	@DisplayName("송금 내역이 생성한다.")
	@Test
	void createTransaction() {
		// given
		TransactionCreateEvent request = new TransactionCreateEvent(
			1L,
			2L,
			1000L,
			IMMEDIATE_TRANSFER,
			WITHDRAW,
			LocalDateTime.now()
		);

		// when
		transactionService.createTransaction(request);

		// then
		Transaction transaction = transactionRepository.findAll().get(0);

		assertThat(transaction)
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(request.senderAccountId(), request.receiverAccountId(), request.amount(), request.type(), request.status());
	}

	@Transactional
	@DisplayName("송금하고 72시간이 지난 송금 내역(Transaction)이 취소된다.")
	@Test
	void processCancelExpiredTransaction() {

	    // given
		Account senderAccount = createAccount(1000L);
		Account receiverAccount = createAccount(1000L);
		LocalDateTime now = LocalDateTime.now();

		List<Transaction> transactions = List.of(
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(73)), // 만료 대상
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 2000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(50)), // 알림 대상
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 3000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(30))  // 알림 대상 아님
		);
		transactionRepository.saveAll(transactions);

	    // when
		transactionService.processCancelExpiredTransactions();

	    // then
		List<Transaction> result = transactionRepository.findTransactionByStatus(CANCEL, 10);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getAmount()).isEqualTo(1000L);
	}

	@DisplayName("송금 마감까지 24시간 남은 송금 내역에 대해서 receiver에게 알림을 전송한다.")
	@Test
	void processRemindNotification() {
	    // given
		Account senderAccount = createAccount(1000L);
		Account receiverAccount = createAccount(1000L);
		LocalDateTime now = LocalDateTime.now();

		List<Transaction> transactions = List.of(
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(73)), // 만료 대상
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 2000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(50)), // 알림 대상
			Transaction.create(senderAccount.getId(), receiverAccount.getId(), 3000L, PENDING_TRANSFER, PENDING_DEPOSIT, now.minusHours(30))  // 알림 대상 아님
		);
		transactionRepository.saveAll(transactions);

	    // when
	    transactionService.processRemindNotifications();

	    // then
		verify(notificationService, times(1)).sendRemindNotification(any());

	}

	private Account createAccount(long money) {
		Account senderAccount = Account.create(money);
		accountRepository.save(senderAccount);
		return senderAccount;
	}


}