package org.c4marathon.assignment.account.domain.repository;

import org.c4marathon.assignment.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
