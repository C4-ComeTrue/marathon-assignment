package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.InsufficientBalanceException;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.c4marathon.assignment.global.util.Const.DEFAULT_BALANCE;

class AccountServiceTest extends IntegrationTestSupport {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAllInBatch();
        savingAccountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

    }

    @DisplayName("메인 계좌를 생성한다.")
    @Test
    void createAccount() throws Exception {
        // given
        Member member = Member.create("test@test.com", "테스트", "testPassword");
        memberRepository.save(member);

        // when
        accountService.createAccount(member.getId());

        // then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getAccountId()).isNotNull();

        Account account = accountRepository.findById(updatedMember.getAccountId()).orElseThrow();
        assertThat(account).isNotNull();
    }
    @DisplayName("메인 계좌에 돈을 충전한다.")
    @Test
    void chargeMoney() throws Exception {
        // given
        Account account =Account.create(DEFAULT_BALANCE);
        accountRepository.save(account);

        long chargeAmount = 50_000L;

        // when
        accountService.chargeMoney(account.getId(), chargeAmount);

        // then
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getMoney()).isEqualTo(50_000L);
        assertThat(updatedAccount.getChargeLimit()).isEqualTo(2_950_000L);
    }

    @DisplayName("일일 충전 한도를 넘어가는 금액을 충전 시도할 경우 예외가 발생한다.")
    @Test
    void chargeMoneyWithDailyLimitExceeded() throws Exception {
        // given
        Account account = Account.create(DEFAULT_BALANCE);
        accountRepository.save(account);

        long chargeAmount = 3_500_000L;

        // when // then
        assertThatThrownBy(() -> accountService.chargeMoney(account.getId(), chargeAmount))
                .isInstanceOf(DailyChargeLimitExceededException.class);
    }


    @DisplayName("메인 계좌에서 적금 계좌로 송금한다.")
    @Test
    void sendToSavingAccount() throws Exception {
        // given
        Member member = Member.create("test@test.com", "테스트", "testPassword");
        memberRepository.save(member);

        Account account = Account.create(10000L);
        member.setMainAccountId(account.getId());
        accountRepository.save(account);

        SavingAccount savingAccount = SavingAccount.create(1000L, member);
        savingAccountRepository.save(savingAccount);

        long sendMoney = 5_000L;

        // when
        accountService.sendToSavingAccount(account.getId(), savingAccount.getId(), sendMoney);

        // then
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        SavingAccount updatedSavingAccount = savingAccountRepository.findById(savingAccount.getId()).orElseThrow();

        assertThat(updatedAccount.getMoney()).isEqualTo(5_000L);
        assertThat(updatedSavingAccount.getBalance()).isEqualTo(6_000L);
    }

    @DisplayName("송금 시도할 때 메인 계좌 잔액이 부족하면 예외가 발생한다.")
    @Test
    void sendToSavingAccountWithInsufficientBalance() throws Exception {
        Member member = Member.create("test@test.com", "테스트", "testPassword");
        memberRepository.save(member);

        Account account = Account.create(10000L);
        member.setMainAccountId(account.getId());
        accountRepository.save(account);

        SavingAccount savingAccount = SavingAccount.create(1000L, member);
        savingAccountRepository.save(savingAccount);

        long sendMoney = 20_000L;

        // when & then
        assertThatThrownBy(() -> accountService.sendToSavingAccount(account.getId(), savingAccount.getId(), sendMoney))
                .isInstanceOf(InsufficientBalanceException.class);
    }

}