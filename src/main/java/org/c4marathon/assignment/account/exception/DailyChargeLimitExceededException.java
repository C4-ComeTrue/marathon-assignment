package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class DailyChargeLimitExceededException extends CustomException {

    public DailyChargeLimitExceededException() {
        super(ErrorCode.CHARGE_LIMIT_EXCEEDED);
    }
}
