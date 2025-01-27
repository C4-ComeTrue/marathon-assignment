package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final ErrorCode errorCode;

	public BaseException(final ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
