package org.c4marathon.assignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private long id;

	@Column(name = "username", nullable = false, length = 30)
	private String username;

	@Column(name = "email", nullable = false, length = 30, unique = true)
	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Size(max = 30, message = "이메일은 최대 30자까지 입력 가능합니다.")
	@Email(message = "이메일 형식이 잘못되었습니다.")
	private String email;

	public User(String username, String email) {
		this.username = username;
		this.email = email;
	}

}
