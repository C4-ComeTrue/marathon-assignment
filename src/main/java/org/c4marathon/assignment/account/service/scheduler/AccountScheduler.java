package org.c4marathon.assignment.account.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.c4marathon.assignment.global.util.Const.CHARGE_LIMIT;

@Service
@RequiredArgsConstructor
public class AccountScheduler {
    private final JdbcTemplate jdbcTemplate;

    /**
     * ChargeLimit 가 3_000_000이 아닌 Account 를 모두 조회해서 일일 한도를 매일 0시에 초기화한다.
     * 배치 업데이트를 위해 JdbcTemplate 를 사용했다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetChargeLimit() {
        int batchSize = 1000;
        Long lastCursorId = 0L;

        while (true) {
            List<Long> accountIds = jdbcTemplate.query(
                    "SELECT account_id FROM account WHERE charge_limit < ? AND account_id > ? ORDER BY account_id ASC LIMIT ?",
                    new Object[] { CHARGE_LIMIT, lastCursorId, batchSize },
                    (rs, rowNum) -> rs.getLong("account_id")
            );

            if (accountIds.isEmpty()) {
                break;
            }
            jdbcTemplate.batchUpdate(
                    "UPDATE account SET charge_limit = ? WHERE account_id = ?",
                    accountIds,
                    batchSize,
                    (ps, accountId) -> {
                        ps.setLong(1, CHARGE_LIMIT);
                        ps.setLong(2, accountId);
                    }
            );

            lastCursorId = accountIds.get(accountIds.size() - 1);
        }
    }
}
