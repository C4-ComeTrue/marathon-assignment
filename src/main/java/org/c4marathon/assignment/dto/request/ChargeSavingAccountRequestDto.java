package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChargeSavingAccountRequestDto(
	@NotNull(message = "메인계좌 id를 입력해주세요.")
	long mainAccountId,
	@NotNull(message = "적금계좌 id를 입력해주세요.")
	long savingAccountId,
	@NotNull(message = "충전 할 금액을 입력해주세요.")
	@PositiveOrZero(message = "0원 이상을 입력해주세요.")
	long money
) {
}
