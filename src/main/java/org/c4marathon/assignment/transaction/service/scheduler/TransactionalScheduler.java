package org.c4marathon.assignment.transaction.service.scheduler;

import org.c4marathon.assignment.transaction.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionalScheduler {
	private final TransactionService transactionService;

	//알맞은 시간 설정해야함
	@Scheduled(fixedRate = 10000)
	public void cancelExpiredTransactions() {
		transactionService.processCancelExpiredTransactions();
	}

	@Scheduled(fixedRate = 10000)
	public void remindNotificationTransactions() {
		transactionService.processRemindNotifications();
	}

}
