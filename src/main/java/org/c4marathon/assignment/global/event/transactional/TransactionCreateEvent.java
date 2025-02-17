package org.c4marathon.assignment.global.event.transactional;

import java.time.LocalDateTime;

import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.TransactionType;

public record TransactionCreateEvent(
	Long senderAccountId,
	Long receiverAccountId,
	long amount,
	TransactionType type,
	TransactionStatus status,
	LocalDateTime sendTime
) {

}
