package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

public record SavingAccountRequestDto(
	long mainAccountId,
	@PositiveOrZero(message = "0원 이상을 입력해주세요.")
	long balance,
	@DecimalMin(value = "0.0", inclusive = true, message = "0.0 이상 입력해주세요.")
	double rate
) {
}
