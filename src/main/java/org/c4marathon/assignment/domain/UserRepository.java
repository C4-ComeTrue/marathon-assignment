package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

	@Modifying
	@Query("UPDATE User u SET u.dayWithdrawLimit = 0 WHERE u.id = :id")
	int initializeWithdrawLimit(Long id);

	@Modifying
	@Query("UPDATE User u SET u.dayWithdraw = u.dayWithdraw + :money, u.lastWithdrawDate = :withdrawTime WHERE u.id = :id AND u.dayWithdraw + :money <= u.dayWithdrawLimit")
	int withdraw(Long id, long money, LocalDateTime withdrawTime);
}
