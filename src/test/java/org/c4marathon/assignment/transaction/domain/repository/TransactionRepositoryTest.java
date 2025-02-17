package org.c4marathon.assignment.transaction.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.exception.NotFoundTransactionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class TransactionRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	void setUp() {
		List<Transaction> transactions = List.of(
			Transaction.create(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(2L, 3L, 2000L, IMMEDIATE_TRANSFER, SUCCESS_DEPOSIT, LocalDateTime.now()),
			Transaction.create(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now()),
			Transaction.create(4L, 5L, 4000L, IMMEDIATE_TRANSFER, FAILED_DEPOSIT, LocalDateTime.now()),
			Transaction.create(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW, LocalDateTime.now())
		);
		transactionRepository.saveAll(transactions);
	}

	@AfterEach
	void tearDown() {
		transactionRepository.deleteAllInBatch();
	}

	@Transactional
	@DisplayName("lastId가 없으면 지정된 pageSize만큼 조회한다.")
	@Test
	void findTransactionByStatus() {
	    // given
		TransactionStatus status = WITHDRAW;
		int pageSize = 3;

		// when
		List<Transaction> result = transactionRepository.findTransactionByStatus(status,
			pageSize);
		// then
		assertThat(result).hasSize(3);

		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(1))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(2))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}


	@Transactional
	@DisplayName("특정 상태의 트랜잭션을 ID 기반 커서 페이징으로 조회한다.")
	@Test
	void findTransactionByStatusWithLastId() {
		// given
		TransactionStatus status = WITHDRAW;
		int pageSize = 2;

		Long lastId = transactionRepository.findTransactionByStatus(status, 1)
			.stream()
			.findFirst()
			.map(Transaction::getId)
			.orElseThrow(NotFoundTransactionException::new);

		// when
		List<Transaction> result = transactionRepository.findTransactionByStatusWithLastId(status,
			lastId, pageSize);

		for (Transaction transaction : result) {
			System.out.println(transaction.getId());
		}

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(3L, 5L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);

		assertThat(result.get(1))
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(5L, 6L, 4000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

	@Transactional
	@DisplayName("transactionId를 통해 Transaction을 조회한다.")
	@Test
	void findTransactionByTransactionId() {

	    // when
		List<Transaction> allTransactions = transactionRepository.findAll();
		Long targetId = allTransactions.get(0).getId();
		Transaction findTransaction = transactionRepository.findTransactionalByTransactionIdWithLock(targetId)
			.orElseThrow(NotFoundTransactionException::new);

		// then
		assertThat(findTransaction)
			.extracting("senderAccountId", "receiverAccountId", "amount", "type", "status")
			.containsExactly(1L, 2L, 1000L, IMMEDIATE_TRANSFER, WITHDRAW);
	}

}