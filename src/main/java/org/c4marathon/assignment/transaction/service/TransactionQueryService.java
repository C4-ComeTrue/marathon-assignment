package org.c4marathon.assignment.transaction.service;

import java.util.List;

import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {
	private final TransactionRepository transactionRepository;

	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public List<Transaction> findTransactionByStatusWithLastId(TransactionStatus status, Long lastId, int size) {
		if (lastId == null) {
			return transactionRepository.findTransactionByStatus(status, size);
		}
		return transactionRepository.findTransactionByStatusWithLastId(status, lastId, size);
	}
}
