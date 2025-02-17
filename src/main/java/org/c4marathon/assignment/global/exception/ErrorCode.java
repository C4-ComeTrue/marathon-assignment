package org.c4marathon.assignment.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member
    DUPLICATE_EMAIL(400, "이미 가입한 이메일입니다."),
    NOT_FOUND_MEMBER(404, "조회된 멤버가 없습니다."),
    INVALID_PASSWORD(400, "잘못된 비밀번호 입니다."),

    // Account
    NOT_FOUND_ACCOUNT(404, "조회된 계좌가 없습니다."),
    CHARGE_LIMIT_EXCEEDED(400, "일일 충전 한도를 초과했습니다."),
    RETRY_LIMIT_EXCEEDED(500, "충전이 로직이 충돌났습니다." ),
    INSUFFICIENT_BALANCE(400, "잔액이 부족합니다."),

    // Transactional
    NOT_FOUND_TRANSACTION(404, "조회된 송금 내역이 없습니다."),
    UNAUTHORIZED_TRANSACTION(403, "해당 송금 취소 권한이 없습니다."),
    INVALID_TRANSACTION_STATUS(400, "해당 송금 내역은 취소할 수 없습니다.");
    private final int status;
    private final String message;
}
