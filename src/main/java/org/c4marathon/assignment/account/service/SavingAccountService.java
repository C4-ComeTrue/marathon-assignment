package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.dto.SavingAccountCreateResponse;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.c4marathon.assignment.global.util.Const.DEFAULT_BALANCE;

@Service
@RequiredArgsConstructor
public class SavingAccountService {
    private final SavingAccountRepository savingAccountRepository;
    private final MemberRepository memberRepository;

    /**
     * 적금 계좌를 생성한다.
     * @param memberId
     * @return
     */
    @Transactional
    public SavingAccountCreateResponse createSavingAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        SavingAccount savingAccount = SavingAccount.create(DEFAULT_BALANCE, member);
        savingAccountRepository.save(savingAccount);

        return new SavingAccountCreateResponse(savingAccount.getId());
    }
}
