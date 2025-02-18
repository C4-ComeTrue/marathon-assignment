package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

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
public class MainAccount extends BaseEntity {
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

	@Column(name = "charge_limit", nullable = false)
	private long limit;

	@Column(name = "charge_date", nullable = false)
	private LocalDateTime chargeDate;

	public MainAccount(User user, String accountNumber, long balance, long limit, LocalDateTime chargeDate) {
		this.user = user;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.limit = limit;
		this.chargeDate = chargeDate;
	}

	public void chargeMoney(long money) {
		this.balance += money;
		this.limit -= money;
	}

	public void updateBalance(long money) {
		this.balance += money;
	}

	public void updateLimit(long limit) {
		this.limit += limit;
	}

	public void updateChargeDate() {
		this.chargeDate = LocalDateTime.now();
	}

	public void withdrawMoney(long money) {
		this.balance -= money;
	}

	public boolean checkBalanceAvailability(long money) {
		return this.balance >= money;
	}

}
