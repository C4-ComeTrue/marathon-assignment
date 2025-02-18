package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
	@NotBlank
	@Size(min = 2, max = 30)
	String username,
	@NotBlank(message = "이메일을 입력해주세요.")
	@Size(max = 30, message = "이메일은 최대 30자까지 입력 가능합니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email
) {
}
