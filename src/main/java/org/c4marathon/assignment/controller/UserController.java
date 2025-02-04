package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.dto.SuccessNonDataResponse;
import org.c4marathon.assignment.common.exception.enums.SuccessCode;
import org.c4marathon.assignment.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/signup")
	public SuccessNonDataResponse signUp(@RequestBody @Valid SignUpRequestDto requestDto){
		userService.signUp(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.CREATE_MAIN_ACCOUNT_SUCCESS);
	}
}
