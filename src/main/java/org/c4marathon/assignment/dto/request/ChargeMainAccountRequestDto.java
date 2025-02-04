package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ChargeMainAccountRequestDto(
	long mainAccountId,
	@PositiveOrZero(message = "0원 이상을 입력해주세요.")
	long money
) {
}
