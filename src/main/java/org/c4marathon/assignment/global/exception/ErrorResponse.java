package org.c4marathon.assignment.global.exception;

public record ErrorResponse(
        int status,
        String message
) {

}
