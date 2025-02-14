package org.c4marathon.assignment.account.service.scheduler;

import static org.c4marathon.assignment.transactional.domain.TransactionalStatus.*;

import java.util.List;

import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.service.TransactionalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositScheduler {
	private final DepositService depositService;
	private final TransactionalService transactionalService;
	private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);
	public final static int PAGE_SIZE = 100;

	@Scheduled(fixedRate = 10000)
	public void deposits() {
		threadPoolExecutor.init();

		Long lastId = null;
		while (true) {
			List<TransferTransactional> transactionals = transactionalService.findTransactionalByStatusWithLastId(
				WITHDRAW, lastId, PAGE_SIZE);

			if (transactionals == null || transactionals.isEmpty()) {
				break;
			}

			lastId = transactionals.get(transactionals.size() - 1).getId();

			for (TransferTransactional transactional : transactionals) {
				threadPoolExecutor.execute(() -> depositService.successDeposit(transactional));
			}

			try {
				threadPoolExecutor.waitToEnd();
			} catch (Exception e) {
				log.error("스레드 풀 실행 중 예외 발생 : {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
	 *  나중에 멀티 스레드 성능 테스트 후 결정
	 */
	@Scheduled(fixedRate = 12000)
	public void retryDeposit() {
		Long lastId = null;
		while (true) {
		List<TransferTransactional> transactionals = transactionalService.findTransactionalByStatusWithLastId(
			FAILED_DEPOSIT, lastId, PAGE_SIZE);

			if (transactionals == null || transactionals.isEmpty()) {
				break;
			}

			lastId = transactionals.get(transactionals.size() - 1).getId();

			for (TransferTransactional transactional  : transactionals) {
				depositService.failedDeposit(transactional);
			}
		}
	}
}
