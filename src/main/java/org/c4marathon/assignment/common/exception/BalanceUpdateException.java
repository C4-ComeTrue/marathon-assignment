package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.exception.enums.ErrorCode;

public class BalanceUpdateException extends BaseException {
	public BalanceUpdateException(ErrorCode errorCode) {
      super(errorCode);
    }
}
