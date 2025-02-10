package org.c4marathon.assignment.domain.dto.response;

public record TransferResult(String senderAccountNumber, String receiverAccountNumber, long money) {
}
