package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

public record ChargeSavingAccountRequestDto(
	long mainAccountId,
	long savingAccountId,
	@PositiveOrZero(message = "0원 이상을 입력해주세요.")
	long money
) {
}
