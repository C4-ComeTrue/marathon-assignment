package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
	static final long DEFAULT_WITHDRAW_LIMIT = 3_000_000L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "email", unique = true, nullable = false, length = 50)
	private String email;

	@Column(name = "day_withdraw_limit", nullable = false)
	private long dayWithdrawLimit;

	@Column(name = "day_withdraw", nullable = false)
	private long dayWithdraw;

	@Column(name = "last_withdraw_date")
	private LocalDateTime lastWithdrawDate;

	@Builder
	private User(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
		String email, Long dayWithdrawLimit, long dayWithdraw, LocalDateTime lastWithdrawDate) {
		super(createdAt, updatedAt, deletedAt);
		this.email = email;
		this.dayWithdrawLimit = dayWithdrawLimit != null ? dayWithdrawLimit : DEFAULT_WITHDRAW_LIMIT;
		this.dayWithdraw = dayWithdraw;
		this.lastWithdrawDate = lastWithdrawDate != null ? lastWithdrawDate : LocalDateTime.now();
	}
}
