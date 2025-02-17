package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.exception.enums.ErrorCode;

public class NotFoundException extends BaseException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
