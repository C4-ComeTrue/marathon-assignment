package org.c4marathon.assignment.transaction.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class InvalidTransactionStatusException extends CustomException {

	public InvalidTransactionStatusException() {
		super(ErrorCode.INVALID_TRANSACTION_STATUS);
	}
}
