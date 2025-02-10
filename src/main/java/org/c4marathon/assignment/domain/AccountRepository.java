package org.c4marathon.assignment.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
	@Modifying
	@Query("UPDATE Account a SET a.balance = a.balance + :money WHERE a.accountNumber = :accountNumber AND a.balance + :money >= 0")
	int updateBalance(@Param("accountNumber") String accountNumber, @Param("money") long money);

	Optional<Account> findByAccountNumber(String accountNumber);

	@Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.isMain = true")
	Optional<Account> findMainAccount(@Param("userId") Long userId);
}
