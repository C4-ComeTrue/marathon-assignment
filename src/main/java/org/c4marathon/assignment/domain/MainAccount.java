package org.c4marathon.assignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "main_account")
@Getter
@NoArgsConstructor
public class MainAccount extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "main_account_id", nullable = false)
	private long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "account_number", nullable = false, length = 15)
	private String accountNumber;

	@Column(name = "balance", nullable = false)
	private long balance;

	public MainAccount(User user, String accountNumber, long balance){
		this.user = user;
		this.accountNumber = accountNumber;
		this.balance = balance;
	}

	public void chargeMoney(long money) {
		this.balance += money;
	}

	public void withdrawMoney(long money){
		this.balance -= money;
	}

	public boolean checkBalanceAvailability(long money){
		return this.balance >= money;
	}

}
