package org.c4marathon.assignment.repository;

import java.util.Optional;
import org.c4marathon.assignment.domain.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM SavingAccount m WHERE m.id = :accountId")
	Optional<SavingAccount> findByIdWithXLock(@Param("accountId") Long accountId);
}
