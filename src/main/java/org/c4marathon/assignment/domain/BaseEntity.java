package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

	@Column(name = "created_at", nullable = false, columnDefinition = "datetime")
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false, columnDefinition = "datetime")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at", columnDefinition = "datetime")
	private LocalDateTime deletedAt;

	protected BaseEntity(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
		this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
		this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
		this.deletedAt = deletedAt;
	}
}
