package org.c4marathon.assignment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("""
		    SELECT t 
		    FROM Transaction t 
		    WHERE t.status = 'PENDING' 
		    AND t.pendingDate <= :endDate
		    AND t.mailSent = FALSE
		    ORDER BY t.pendingDate, t.id
		    LIMIT :size
		""")
	List<Transaction> findRemindableTransactions(
		@Param("endDate") LocalDateTime endDate,
		@Param("size") int size
	);

	@Query("""
		    SELECT t 
		    FROM Transaction t 
		    WHERE t.status = 'PENDING' 
		    AND t.pendingDate <= :endDate
		    AND t.mailSent = FALSE
		    AND (t.pendingDate > :lastPendingDate 
		         OR (t.pendingDate = :lastPendingDate AND t.id > :lastTransactionId))
		    ORDER BY t.pendingDate, t.id
		    LIMIT :size
		""")
	List<Transaction> findRemindableTransactionsWithCursor(
		@Param("endDate") LocalDateTime endDate,
		@Param("lastPendingDate") LocalDateTime lastPendingDate,
		@Param("lastTransactionId") Long lastTransactionId,
		@Param("size") int size
	);

	@Modifying
	@Query("""
		    UPDATE Transaction t
		    SET t.mailSent = TRUE
		    WHERE t.id IN :transactionIds
		""")
	void updateMailSent(@Param("transactionIds") List<Long> transactionIds);

	@Query("""
		    SELECT t 
		    FROM Transaction t 
		    WHERE t.status = 'PENDING' 
		    AND t.pendingDate <= :expirationTime
		    ORDER BY t.pendingDate, t.id
		    LIMIT :size
		""")
	List<Transaction> findExpiredTransactions(
		@Param("expirationTime") LocalDateTime expirationTime,
		@Param("size") int size
	);

	@Query("""
		    SELECT t 
		    FROM Transaction t 
		    WHERE t.status = 'PENDING' 
		    AND t.pendingDate <= :expirationTime
		   	AND (t.pendingDate > :lastPendingDate
		             OR (t.pendingDate = :lastPendingDate AND t.id > :lastTransactionId))
		    ORDER BY t.pendingDate, t.id
		    LIMIT :size
		""")
	List<Transaction> findExpiredTransactionsWithCursor(
		@Param("expirationTime") LocalDateTime expirationTime,
		@Param("lastPendingDate") LocalDateTime lastPendingDate,
		@Param("lastTransactionId") Long lastTransactionId,
		@Param("size") int size
	);

	@Modifying
	@Query("""
		    UPDATE Transaction t 
		    SET t.status = 'CANCELED' 
		    WHERE t.id IN :transactionIds
		""")
	void updateExpiredTransactions(@Param("transactionIds") List<Long> transactionIds);
}
