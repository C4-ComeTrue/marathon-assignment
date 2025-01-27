package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.request.ChargeMainAccountRequestDto;
import org.c4marathon.assignment.service.MainAccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account/main")
@RequiredArgsConstructor
public class MainAccountController {

	private final MainAccountService mainAccountService;

	@PostMapping("/charge")
	public void chargeMainAccount(@RequestBody @Valid ChargeMainAccountRequestDto requestDto){
		mainAccountService.chargeMainAccount(requestDto);
	}
}
