package org.c4marathon.assignment.domain;

import org.c4marathon.assignment.domain.enums.TransactionStatus;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_member")
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_id", nullable = false)
	private Settlement settlement;

	public SettlementMember(long accountId, long amount, Settlement settlement) {
		this.accountId = accountId;
		this.amount = amount;
		this.settlement = settlement;
	}

	public void updateAmount(long money) {
		this.amount += money;
	}
}
