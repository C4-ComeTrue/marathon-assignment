package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.InsufficientBalanceException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.c4marathon.assignment.global.util.Const.DEFAULT_BALANCE;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createAccount(Long memberId) {
        Account account = Account.create(DEFAULT_BALANCE);
        accountRepository.save(account);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        member.setMainAccountId(account.getId());

        memberRepository.save(member);
    }

    /**
     * 메인 계좌에 돈을 충전하다.
     * 한 번에 메인 계좌에다가 충전을 여러 번 할 수도 있다. 어떻게 관리해야하나?
     * 나의 외부계좌에서 내 메인 계좌로 충전하는 것이 충돌 가능성이 그렇게 높지는 않을 것 같다.
     * 굳이 비관적 락을 사용할 필요는 없을 것 같다. 낙관적 락을 사용
     * @param accountId
     * @param money
     */
    //기본값 -> Repeatable Read
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void chargeMoney(Long accountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isCharge(money)) {
            throw new DailyChargeLimitExceededException();
        }

        //OptimisticLockException 예외 발생시 globalHandlerException 에서 잡자
        account.chargeAccount(money);
        accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void sendToSavingAccount(Long accountId, Long savingAccountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isSend(money)) {
            throw new InsufficientBalanceException();
        }

        SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
                .orElseThrow(NotFoundAccountException::new);

        account.minusMoney(money);
        accountRepository.save(account);

        savingAccount.addMoney(money);
        savingAccountRepository.save(savingAccount);
    }

}
