package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.NotNull;

public record SavingAccountRequestDto(
	@NotNull(message = "메인계좌 id를 입력해주세요.")
	long mainAccountId,
	@NotNull(message = "초기금액을 입력해주세요.")
	long balance,
	@NotNull(message = "이율을 입력해주세요.")
	double rate
) {
}
