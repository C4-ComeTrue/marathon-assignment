package org.c4marathon.assignment.domain.dto.response;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.AccountType;

public record CreatedAccountInfo(String accountNumber, LocalDateTime createdAt, AccountType accountType, long balance,
								 boolean isMain) {
}
