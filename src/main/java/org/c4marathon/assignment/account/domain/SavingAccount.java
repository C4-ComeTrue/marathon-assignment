package org.c4marathon.assignment.account.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.global.entity.BaseEntity;
import org.c4marathon.assignment.member.domain.Member;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SavingAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saving_account_id")
    private Long id;

    @Column(nullable = false)
    private long balance;

    /* step 5에 추가
    @Column(nullable = false)
    private long rate;

    @Column(nullable = false)
    private String productType;
    */

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Version
    private Integer version;

    @Builder
    private SavingAccount(long balance, Member member) {
        this.balance = balance;
        this.member = member;
    }

    public static SavingAccount create(long balance, Member member) {
        return SavingAccount.builder()
                .balance(balance)
                .member(member)
                .build();
    }

    public void addMoney(long money) {
        this.balance += money;
    }
}
