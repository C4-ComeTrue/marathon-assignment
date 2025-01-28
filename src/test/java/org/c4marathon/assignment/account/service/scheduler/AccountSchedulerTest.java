package org.c4marathon.assignment.account.service.scheduler;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.c4marathon.assignment.global.util.Const.CHARGE_LIMIT;

class AccountSchedulerTest extends IntegrationTestSupport {
    @Autowired
    private AccountScheduler accountScheduler;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO account (account_id, money, charge_limit) VALUES (?, ?, ?)", 1L, 1000L, 3_000_000L);
        jdbcTemplate.update("INSERT INTO account (account_id, money, charge_limit) VALUES (?, ?, ?)", 2L, 1000L, 1_500_000L);
        jdbcTemplate.update("INSERT INTO account (account_id, money, charge_limit) VALUES (?, ?, ?)", 3L, 1000L, 50_000L);
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAllInBatch();
    }

    @DisplayName("배치로 계좌 충전 한도를 초기화 한다.")
    @Test
    void resetChargeLimit() throws Exception {
        // when
        accountScheduler.resetChargeLimit();

        // then
        Account updatedAccount1 = accountRepository.findById(1L).orElseThrow();
        Account updatedAccount2 = accountRepository.findById(2L).orElseThrow();
        Account updatedAccount3 = accountRepository.findById(3L).orElseThrow();

        assertThat(updatedAccount1.getChargeLimit()).isEqualTo(CHARGE_LIMIT);
        assertThat(updatedAccount2.getChargeLimit()).isEqualTo(CHARGE_LIMIT);
        assertThat(updatedAccount3.getChargeLimit()).isEqualTo(CHARGE_LIMIT);
    }
}