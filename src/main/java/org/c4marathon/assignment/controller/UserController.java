package org.c4marathon.assignment.controller;

import java.util.List;

import org.c4marathon.assignment.application.UserService;
import org.c4marathon.assignment.domain.dto.request.SettlementRequest;
import org.c4marathon.assignment.domain.dto.response.SettlementResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@PostMapping("/settle")
	public ResponseEntity<List<SettlementResult>> settle(@RequestBody SettlementRequest request) {
		return ResponseEntity.ok(userService.settle(request));
	}
}
