package org.c4marathon.assignment.dto.request;

import java.util.List;

import org.c4marathon.assignment.domain.enums.SettlementType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SettlementRequestDto(
	long requestAccountId,
	@Positive(message = "양수값으로 입력해주세요.")
	int totalAmount,
	@NotNull
	SettlementType type,
	@NotEmpty(message = "정산 멤버 리스트는 비어있을 수 없습니다.")
	List<Long> settlementMemberIds
) {
}
