package org.c4marathon.assignment.member.domain.repository;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.member.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("중복된 이메일을 가진 유저가 있으면 true을 반환한다.")
    @Test
    void validateDuplicateEmail() {
        // given
        Member member = Member.create("test@naver.com", "테스트", "test");
        memberRepository.save(member);

        // when
        boolean result = memberRepository.existsByEmail("test@naver.com");

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("중복된 이메일을 가진 유저가 있으면 false을 반환한다.")
    @Test
    void validateDuplicateEmail_2() {
        // given
        Member member = Member.create("test@naver.com", "테스트", "test");
        memberRepository.save(member);

        // when
        boolean result = memberRepository.existsByEmail("test1@naver.com");

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("이메일로 멤버를 찾는다.")
    @Test
    void findMemberByEmail() {
        // given
        Member member = Member.create("test@naver.com", "테스트", "test");
        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByEmail("test@naver.com");

        // then
        assertThat(findMember.get())
                .extracting("email", "name", "password")
                .contains("test@naver.com", "테스트", "test");
    }

    @DisplayName("AccountId로 멤버를 찾는다.")
    @Test
    void findMemberByAccountId() {
        // given
        Member member = Member.create("test@naver.com", "테스트", "test");
        member.setMainAccountId(1L);
        memberRepository.save(member);

        // when
        Optional<Member> findMember = memberRepository.findByAccountId(1L);

        // then
        assertThat(findMember.get())
            .extracting("email", "name", "password", "accountId")
            .contains("test@naver.com", "테스트", "test", 1L);
    }
}