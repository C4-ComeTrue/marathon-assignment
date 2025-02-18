package org.c4marathon.assignment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.Transaction;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryCursor {
	private final TransactionRepository transactionRepository;

	/**
	 *  주어진 endDate전까지 모든 Transaction 정보를 size만큼씩 가져온다.
	 */
	public List<Transaction> findRemindableTransactions(LocalDateTime endDate,LocalDateTime pendingDate, Long lastDateId, int size) {
		if (lastDateId == null) {
			return transactionRepository.findRemindableTransactions(endDate, size);
		}
		return transactionRepository.findRemindableTransactionsWithCursor(endDate, pendingDate, lastDateId, size);
	}

	public List<Transaction> findExpiredTransactions(LocalDateTime endDate,LocalDateTime pendingDate, Long lastDateId, int size) {
		if (lastDateId == null) {
			return transactionRepository.findExpiredTransactions(endDate, size);
		}
		return transactionRepository.findExpiredTransactionsWithCursor(endDate, pendingDate, lastDateId, size);
	}
}
