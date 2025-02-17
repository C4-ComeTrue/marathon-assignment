package org.c4marathon.assignment.repository;

import java.util.Optional;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT sm FROM SettlementMember sm WHERE sm.settlement.id = :settlementId and sm.accountId = :memberAccountId")
	Optional<SettlementMember> findByAccountIdWithXLock(@Param("settlementId") Long settlementId, @Param("memberAccountId") Long memberAccountId);
}
