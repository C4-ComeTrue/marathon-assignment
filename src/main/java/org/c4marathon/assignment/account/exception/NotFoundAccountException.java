package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;

public class NotFoundAccountException extends CustomException {
    public NotFoundAccountException() {
        super(ErrorCode.NOT_FOUND_ACCOUNT);
    }
}
