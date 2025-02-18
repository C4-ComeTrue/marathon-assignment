package org.c4marathon.assignment.service;

import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
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
		User user = new User(requestDto.username(), requestDto.email());
		userRepository.save(user);
		mainAccountService.createMainAccount(user);
	}

	public User getUser(long userId){
		return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER));
	}

}
