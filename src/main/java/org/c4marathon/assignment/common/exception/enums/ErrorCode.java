package org.c4marathon.assignment.common.exception.enums;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	//404
	NOT_FOUND_MAIN_ACCOUNT(HttpStatus.NOT_FOUND, "메인 계좌가 존재하지 않습니다."),
	NOT_FOUND_SAVING_ACCOUNT(HttpStatus.NOT_FOUND, "적금 계좌가 존재하지 않습니다."),
	NOT_FOUND_SETTLEMENT_MEMBER(HttpStatus.NOT_FOUND, "정산 멤버가 존재하지 않습니다."),
	NOT_FOUND_SETTLEMENT(HttpStatus.NOT_FOUND, "정산 정보가 존재하지 않습니다."),
	//409
	DAILY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "일일 한도를 초과했습니다."),
	REDIS_RESET_EXCEPTION(HttpStatus.CONFLICT, "일일 한도 초기화 중입니다."),
	FAILED_BALANCE_UPDATE(HttpStatus.CONFLICT, "잔고 업데이트에 실패했습니다."),
	BALANCE_NOT_ENOUGH(HttpStatus.CONFLICT, "잔고 부족으로 실패했습니다."),
	ALREADY_REMITTANCE_SUCCESS(HttpStatus.CONFLICT, "이미 정산이 완료된 멤버입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
