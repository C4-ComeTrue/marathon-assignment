package org.c4marathon.assignment.repository;

import java.util.List;

import org.c4marathon.assignment.domain.Item;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.domain.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesRepository extends JpaRepository<Sales, Long> {

	List<Sales> findAllBySeller(Member seller);

	@Query("select s from Sales s join fetch OrderItem o where o.item = :item")
	List<Sales> findAllByItem(Item item);

}
