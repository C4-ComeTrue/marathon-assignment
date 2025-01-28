package org.c4marathon.assignment.member.dto;

public record MemberRegisterResponse(
        Long memberId,
        String email
) {
}
