package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.transaction.domain.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WithdrawRequest(

    @NotNull
    Long receiverAccountId,

    @PositiveOrZero
    long money,

    @NotNull
    TransactionType type

) {
}
