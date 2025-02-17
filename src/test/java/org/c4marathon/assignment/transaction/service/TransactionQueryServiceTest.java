package org.c4marathon.assignment.transaction.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class TransactionQueryServiceTest extends IntegrationTestSupport {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private TransactionQueryService transactionQueryService;

	@AfterEach
	void tearDown() {
		transactionRepository.deleteAllInBatch();
	}

	@Transactional
	@DisplayName("lastId가 없을 경우 지정된 pageSize 만큼 조회된다.")
	@Test
	void findTransactionByStatusWithoutLastId() {
	    // given
		int pageSize = 2;
		List<Transaction> transactions = List.of(
			Transaction.create(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(2L, 3L, 2000L, IMMEDIATE_TRANSFER, SUCCESS_DEPOSIT, LocalDateTime.now()),
			Transaction.create(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(4L, 5L, 4000L, IMMEDIATE_TRANSFER, FAILED_DEPOSIT, LocalDateTime.now()),
			Transaction.create(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now())
		);
		transactionRepository.saveAll(transactions);

	    // when
		List<Transaction> result = transactionQueryService.findTransactionByStatusWithLastId(
			WITHDRAW, null, pageSize);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW);
		assertThat(result.get(1))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

	@Transactional
	@DisplayName("lastId가 있을 경우 lastId보다 큰 ID 중 pageSize 만큼 조회한다.")
	@Test
	void findTransactionByStatusWithLastId() {
		// given
		TransactionStatus status = WITHDRAW;
		int pageSize = 2;

		List<Transaction> transactions = List.of(
			Transaction.create(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(2L, 3L, 2000L, IMMEDIATE_TRANSFER, SUCCESS_DEPOSIT, LocalDateTime.now()),
			Transaction.create(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(4L, 5L, 4000L, IMMEDIATE_TRANSFER, FAILED_DEPOSIT, LocalDateTime.now()),
			Transaction.create(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now())
		);
		transactionRepository.saveAll(transactions);

		List<Transaction> findTransactions = transactionRepository.findTransactionByStatus(status, pageSize);

		Long lastId = findTransactions.get(findTransactions.size() - 1).getId();
		// when
		List<Transaction> result = transactionQueryService.findTransactionByStatusWithLastId(status, lastId, pageSize);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

}