package org.c4marathon.assignment.domain;

import org.c4marathon.assignment.domain.enums.SettlementStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class SettlementMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_member_id")
	private Long id;

	@Column(name = "account_id", nullable = false)
	private long accountId;

	@Column(name = "amount", nullable = false)
	private long amount;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SettlementStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_id", nullable = false)
	private Settlement settlement;

	public SettlementMember(long accountId, long amount, SettlementStatus status, Settlement settlement) {
		this.accountId = accountId;
		this.amount = amount;
		this.status = status;
		this.settlement = settlement;
	}

	public void updateAmount(long money) {
		this.amount += money;
	}

	public void updateStatus(SettlementStatus status) {
		this.status = status;
	}
}
