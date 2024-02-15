package org.c4marathon.assignment.domain.entity;

import org.c4marathon.assignment.domain.ChargeLimit;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)   // TEST
@Table(
	indexes = {@Index(name = "account_member_index", columnList = "member_id")}
)
public class Account extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private String name;

	private String accountNumber;            // TODO: Bank Enum 관리

	@NotNull
	private long amount;

	@NotNull
	private int accumulatedChargeAmount;     // 사용자가 1일 동안 누적한 충전 금액 -> 하루 주기로 초기화

	@Enumerated(EnumType.STRING)
	private ChargeLimit chargeLimit = ChargeLimit.DAY_BASIC_LIMIT;

	@Builder
	public Account(Member member, String name, String accountNumber) {
		this.member = member;
		this.name = name;
		this.accountNumber = accountNumber;
	}

	public void charge(int amount) {
		this.amount += amount;
		this.accumulatedChargeAmount += amount;
	}

	public void withdraw(int amount) {
		this.amount -= amount;
	}
}
