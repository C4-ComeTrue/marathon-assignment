package org.c4marathon.assignment.dto.request;

public record RemittanceRequestDto(
	long settlementId,
	long settlementMemberAccountId
) {
}
