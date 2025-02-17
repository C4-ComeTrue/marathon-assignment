package org.c4marathon.assignment.transaction.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	//index(TransactionalStatus, id)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.status = :status AND t.id > :lastId
		ORDER BY t.id
		LIMIT :size
		""")
	List<Transaction> findTransactionByStatusWithLastId(
		@Param("status") TransactionStatus status,
		@Param("lastId") Long lastId,
		@Param("size") int size
	);

	//index(TransactionalStatus, id)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.status = :status
		ORDER BY t.id
		LIMIT :size
		""")
	List<Transaction> findTransactionByStatus(
		@Param("status") TransactionStatus status,
		@Param("size") int size
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.id = :id
		""")
	Optional<Transaction> findTransactionalByTransactionIdWithLock(@Param("id") Long id);
}
