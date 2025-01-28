package org.c4marathon.assignment.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SendToSavingAccountRequest(

        @NotNull(message = "적금 계좌 ID는 필수 입력 값입니다.")
        @Positive(message = "ID는 0보다 커야 합니다.")
        Long savingAccountId,

        @Positive(message = "잘못된 이체 금액입니다.")
        long money
) {
}
