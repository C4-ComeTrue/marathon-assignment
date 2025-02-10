package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.application.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<Boolean> register(@RequestParam String email) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(email));
	}
}
