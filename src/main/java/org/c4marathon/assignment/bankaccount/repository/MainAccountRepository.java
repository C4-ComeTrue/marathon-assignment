package org.c4marathon.assignment.bankaccount.repository;

import java.util.Optional;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MainAccountRepository extends JpaRepository<MainAccount, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ma from MainAccount ma where ma.accountPk = :accountPk")
	Optional<MainAccount> findByPkForUpdate(@Param("accountPk") long accountPk);

	@Modifying(clearAutomatically = true) // 영속성 컨텍스트와 동기화가 필요하다.
	@Query("""
		update MainAccount ma
		set ma.money = ma.money + :chargeMoney
		where ma.accountPk = :accountPk
		""")
	int deposit(@Param("accountPk") long accountPk, @Param("chargeMoney") long chargeMoney);

}
