package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

public record TransferRequestDto(
	long senderAccountId,
	long receiverAccountId,
	@PositiveOrZero(message = "0원 이상을 입력해주세요.")
	long amount
) {
}
