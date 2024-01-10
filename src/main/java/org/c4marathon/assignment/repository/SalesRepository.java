package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesRepository extends JpaRepository<Sales, Long> {
}
