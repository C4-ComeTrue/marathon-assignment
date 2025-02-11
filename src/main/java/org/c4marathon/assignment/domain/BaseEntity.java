package org.c4marathon.assignment.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {
	@NotNull
	@CreatedDate
	@Column(name = "create_date", nullable = false, updatable = false)
	private LocalDateTime createDate;

	@NotNull
	@LastModifiedDate
	@Column(name = "update_date", nullable = false)
	private LocalDateTime updateDate;
}