package org.c4marathon.assignment.account.domain.repository;

import org.c4marathon.assignment.account.domain.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

}
