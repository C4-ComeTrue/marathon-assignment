package org.c4marathon.assignment.service;

import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final MainAccountService mainAccountService;

	@Transactional
	public void signUp(SignUpRequestDto requestDto){
		User user = new User(requestDto.username());
		userRepository.save(user);
		mainAccountService.createMainAccount(user);
	}
}
