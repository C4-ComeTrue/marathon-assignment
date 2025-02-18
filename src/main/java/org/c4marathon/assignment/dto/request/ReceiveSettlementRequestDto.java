package org.c4marathon.assignment.dto.request;

public record ReceiveSettlementRequestDto(
	long transactionId,
	long settlementMemberId
) {
}
