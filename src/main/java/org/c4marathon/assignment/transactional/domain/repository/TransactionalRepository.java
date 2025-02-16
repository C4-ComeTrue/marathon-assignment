package org.c4marathon.assignment.transactional.domain.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.transactional.domain.Transaction;
import org.c4marathon.assignment.transactional.domain.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TransactionalRepository extends JpaRepository<Transaction, Long> {

	//index(TransactionalStatus)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.status = :status AND t.id > :lastId
		ORDER BY t.id
		LIMIT :size
		""")
	List<Transaction> findTransactionalByStatusWithLastId(
		@Param("status") TransactionStatus status,
		@Param("lastId") Long lastId,
		@Param("size") int size
	);

	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.status = :status
		ORDER BY t.id
		LIMIT :size

		""")
	List<Transaction> findTransactionalByStatus(
		@Param("status") TransactionStatus status,
		@Param("size") int size
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT t
		FROM Transaction t
		WHERE t.id = :id
		""")
	Optional<Transaction> findTransactionalByTransactionalIdWithLock(@Param("id") Long id);

}
