package org.c4marathon.assignment.common.exception.enums;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {

	CREATE_MAIN_ACCOUNT_SUCCESS(HttpStatus.CREATED, "메인 계좌 생성이 완료되었습니다."),
	CREATE_SAVING_ACCOUNT_SUCCESS(HttpStatus.CREATED, "적금 계좌 생성이 완료되었습니다."),
	CHARGE_MAIN_ACCOUNT_SUCCESS(HttpStatus.OK, "메인 계좌에 충전 완료되었습니다."),
	TRANSFER_SAVING_ACCOUNT_SUCCESS(HttpStatus.OK, "적금 계좌에 송금 완료되었습니다."),
	REQUEST_SETTLEMENT_SUCCESS(HttpStatus.OK, "정산 요청이 완료되었습니다."),
	REQUEST_REMITTANCE_SETTLEMENT_SUCCESS(HttpStatus.OK, "정산 송금이 요청되었습니다."),
	RECIEVE_REMITTANCE_SETTLEMENT_SUCCESS(HttpStatus.OK, "정산 송금 요청을 수락했습니다."),
	REMITTANCE_SETTLEMENT_SUCCESS(HttpStatus.OK, "정산 송금이 완료되었습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
