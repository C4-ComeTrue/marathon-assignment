package org.c4marathon.assignment.domain;

import java.util.List;

import org.c4marathon.assignment.domain.enums.SettlementStatus;
import org.c4marathon.assignment.domain.enums.SettlementType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor
public class Settlement extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id")
	private Long id;

	@Column(name = "request_account_id", nullable = false)
	private Long requestAccountId;

	@Column(name = "total_amount", nullable = false)
	private long totalAmount;

	@Column(name = "remain_amount", nullable = false)
	private long remainAmount;

	@Column(name = "people", nullable = false)
	private int people;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private SettlementType type;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SettlementStatus status;

	@OneToMany(mappedBy = "settlement", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SettlementMember> settlementMembers;

	public Settlement(Long requestAccountId, long totalAmount,long remainAmount, int people, SettlementType type, SettlementStatus status,
		List<SettlementMember> settlementMembers) {
		this.requestAccountId = requestAccountId;
		this.totalAmount = totalAmount;
		this.remainAmount = remainAmount;
		this.people = people;
		this.type = type;
		this.status = status;
		this.settlementMembers = settlementMembers;
	}

	public void addSettlementMembers(List<SettlementMember> members){
		this.settlementMembers = members;
	}

	public void updateRemainAmount(long remainAmount){
		this.remainAmount = remainAmount;
	}

	public void updateStatus(SettlementStatus status){
		this.status = status;
	}

}
