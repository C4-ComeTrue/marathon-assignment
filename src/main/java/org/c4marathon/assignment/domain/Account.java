package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.c4marathon.assignment.global.AccountUtils;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ColumnDefault("0")
	@Column(name = "balance", nullable = false)
	private long balance;

	@Column(name = "account_number", nullable = false, unique = true, length = 50)
	private String accountNumber;

	@Column(name = "is_main", nullable = false)
	private boolean isMain;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false, length = 30)
	private AccountType accountType;

	@Column(name = "user_id", nullable = false)
	private long userId;

	@Builder
	private Account(long balance, String accountNumber, boolean isMain, AccountType accountType, Long userId,
		LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		super(createdAt, updatedAt, deletedAt);
		this.balance = balance;
		this.accountNumber = accountNumber != null ? accountNumber : AccountUtils.getAccountNumber();
		this.isMain = isMain;
		this.accountType = accountType != null ? accountType : AccountType.CHECKING;
		this.userId = userId;
	}
}
