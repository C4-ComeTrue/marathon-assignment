package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.NotNull;

public record SavingAccountRequestDto(
	long mainAccountId,
	long balance,
	double rate
) {
}
