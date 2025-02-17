package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Settlement s WHERE s.id = :id")
	Optional<Settlement> findByIdWithXLock(@Param("id") Long id);
}
