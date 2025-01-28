package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;

	@Transactional
	public boolean register(String email) {
		User createdUser = User.builder().email(email).build();
		userRepository.save(createdUser);

		Account account = Account.builder().isMain(true).userId(createdUser.getId()).build();
		accountRepository.save(account);

		return true;
	}
}
