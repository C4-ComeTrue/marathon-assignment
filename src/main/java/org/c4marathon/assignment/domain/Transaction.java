package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.enums.TransactionStatus;
import org.c4marathon.assignment.domain.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction")
@Getter
@NoArgsConstructor
public class Transaction extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id", nullable = false)
	private long id;

	@Size(max = 20)
	@NotNull
	@Column(name = "sender_account_id", nullable = false, length = 20)
	private long senderAccountId;

	@Size(max = 20)
	@NotNull
	@Column(name = "receiver_account_id", nullable = false, length = 20)
	private long receiverAccountId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "settlement_member_id", nullable = false)
	private SettlementMember settlementMember;

	@Column(name = "amount", nullable = false)
	private long amount;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Enumerated(EnumType.STRING)
	private TransactionType type;

	@Column(name = "pending_mail_sent", nullable = false)
	private boolean mailSent = false;

	@Column(name = "pending_date")
	private LocalDateTime pendingDate;

	public Transaction(long senderAccountId, long receiverAccountId,SettlementMember settlementMember, long amount, TransactionStatus status,
		TransactionType type) {
		this.senderAccountId = senderAccountId;
		this.receiverAccountId = receiverAccountId;
		this.settlementMember = settlementMember;
		this.amount = amount;
		this.status = status;
		this.type = type;
	}

	public void pendingTransaction() {
		this.status = TransactionStatus.PENDING;
		this.pendingDate = LocalDateTime.now();
	}

	public void successTransaction() {
		this.status = TransactionStatus.SUCCESS;
	}

	public void cancelTransaction() {
		this.status = TransactionStatus.CANCELED;
	}

	public void pendingMailSent(){
		this.mailSent = true;
	}

}
