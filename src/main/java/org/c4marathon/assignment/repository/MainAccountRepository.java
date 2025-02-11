package org.c4marathon.assignment.repository;

import java.util.List;
import java.util.Optional;
import org.c4marathon.assignment.domain.MainAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface MainAccountRepository extends JpaRepository<MainAccount, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM MainAccount m WHERE m.id = :accountId")
	Optional<MainAccount> findByIdWithXLock(@Param("accountId") Long accountId);

	@Modifying
	@Query("UPDATE MainAccount m SET m.limit = :chargeLimit WHERE m.id IN :accountIds")
	void batchUpdateChargeLimit(@Param("accountIds") List<Long> accountIds, @Param("chargeLimit") Long chargeLimit);

}
