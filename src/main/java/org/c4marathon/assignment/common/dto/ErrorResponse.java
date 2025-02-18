package org.c4marathon.assignment.common.dto;

import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
	private final int code;
	private final String message;

	public static ErrorResponse error(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.getMessage());
	}

	public static ErrorResponse badRequestError(final String errorMessage) {
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage);
	}
}