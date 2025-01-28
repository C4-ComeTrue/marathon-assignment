package org.c4marathon.assignment.global.session;

import java.io.Serializable;

public record SessionMemberInfo(
    Long memberId,
    String email,
    Long accountId

) implements Serializable {
}
