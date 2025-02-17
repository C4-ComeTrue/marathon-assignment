package org.c4marathon.assignment.global.event.transactional;

import org.c4marathon.assignment.transaction.service.TransactionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionCreateEventListener {

	private final TransactionService transactionService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleTransactionalCreate(TransactionCreateEvent event) {
		transactionService.createTransaction(event);
	}
}
