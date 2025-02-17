package org.c4marathon.assignment.transaction.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class NotFoundTransactionException extends CustomException {
	public NotFoundTransactionException() {
		super(ErrorCode.NOT_FOUND_TRANSACTION);
	}
}
