package org.c4marathon.assignment.member.domain.repository;

import org.c4marathon.assignment.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // index(email)
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByAccountId(Long accountId);
}
