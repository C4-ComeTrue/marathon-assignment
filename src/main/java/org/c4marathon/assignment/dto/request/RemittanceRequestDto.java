package org.c4marathon.assignment.dto.request;

import org.c4marathon.assignment.domain.enums.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RemittanceRequestDto(
	long settlementId,
	long requestAccountId,
	long settlementMemberAccountId,
	@Positive(message = "양수값을 정산해야합니다.")
	long amount,
	@NotNull
	TransactionType type
) {
}
