package org.c4marathon.assignment.transaction.domain;

public enum TransactionStatus {
	WITHDRAW,
	PENDING_DEPOSIT,
	SUCCESS_DEPOSIT,
	FAILED_DEPOSIT,
	CANCEL
}
