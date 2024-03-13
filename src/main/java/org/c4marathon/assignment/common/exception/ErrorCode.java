package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// common
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
	BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	// member
	INVALID_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

	// account
	INVALID_ACCOUNT(HttpStatus.NOT_FOUND, "존재하지 않는 계좌입니다."),
	ACCOUNT_LACK_OF_AMOUNT(HttpStatus.BAD_REQUEST, "계좌에 돈이 부족해 출금에 실패했습니다."),
	INVALID_SAVINGS_TRANSFER(HttpStatus.BAD_REQUEST, "자유 적금 계좌에만 입금할 수 있습니다."),

	// charge
	EXCEED_CHARGE_LIMIT(HttpStatus.BAD_REQUEST, "1일 충전 한도를 넘어 충전이 불가능합니다."),
	INVALID_CHARGE_LINKED_ACCOUNT(HttpStatus.BAD_REQUEST, "주 충전 계좌가 존재하지 않습니다.");


	private final HttpStatus status;
	private final String message;

	public BusinessException businessException() {
		return new BusinessException(this.name(), message);
	}

	public BusinessException businessException(Throwable cause) {
		return new BusinessException(cause, this.name(), message);
	}

	public BusinessException businessException(String debugMessage, Object... debugMessageArgs) {
		return new BusinessException(this.name(), message, String.format(debugMessage, debugMessageArgs));
	}

	public BusinessException businessException(Throwable cause, String debugMessage, Object... debugMessageArgs) {
		return new BusinessException(cause, this.name(), message, String.format(debugMessage, debugMessageArgs));
	}

}
