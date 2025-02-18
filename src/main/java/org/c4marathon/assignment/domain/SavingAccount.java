package org.c4marathon.assignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "saving_account")
@Getter
@NoArgsConstructor
public class SavingAccount extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "saving_account_id", nullable = false)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "main_account_id", nullable = false)
	private MainAccount mainAccount;

	@Column(name = "account_number", nullable = false, length = 15)
	private String accountNumber;

	@Column(name = "balance", nullable = false)
	private long balance;

	@Column(name = "rate", nullable = false)
	private double rate;

	public SavingAccount(MainAccount mainAccount, String accountNumber, long balance, double rate){
		this.mainAccount = mainAccount;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.rate = rate;
	}

	public void chargeMoney(long money) {
		this.balance += money;
	}
}
