package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.dto.SavingAccountCreateResponse;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SavingAccountServiceTest extends IntegrationTestSupport {
    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private SavingAccountService savingAccountService;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        savingAccountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("적금 계좌를 생성한다.")
    @Test
    void createSavingAccount() throws Exception {
        // given
        Member member = Member.create("test@test.com", "테스트", "testPassword");
        memberRepository.save(member);

        // when
        SavingAccountCreateResponse savingAccount = savingAccountService.createSavingAccount(member.getId());

        // then
        assertThat(savingAccount.savingAccountId()).isNotNull();
    }
}