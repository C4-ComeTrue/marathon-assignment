package org.c4marathon.assignment.domain;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	@Modifying
	@Query("UPDATE User u SET u.accCharge = 0 WHERE u.id IN :ids")
	int initChargeLimit(@Param("ids") Collection<Long> ids);

	@Modifying
	@Query("UPDATE User u SET u.accCharge = u.accCharge + :money WHERE u.id = :id AND u.accCharge + :money <= u.chargeLimit")
	int charge(@Param("id") Long id, @Param("money") long money);

	@Query(value = "SELECT * FROM user u WHERE u.id > :cursor LIMIT :limit", nativeQuery = true)
	List<User> findAllByCursor(@Param("cursor") long cursor, @Param("limit") int limit);
}
