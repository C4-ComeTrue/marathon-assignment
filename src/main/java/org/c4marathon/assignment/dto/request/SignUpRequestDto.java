package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
	@NotBlank
	@Size(max = 30)
	String username
) {
}
