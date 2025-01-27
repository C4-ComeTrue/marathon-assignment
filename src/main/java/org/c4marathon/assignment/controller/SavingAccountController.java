package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.dto.request.ChargeSavingAccountRequestDto;
import org.c4marathon.assignment.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.service.SavingAccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account/saving")
@RequiredArgsConstructor
public class SavingAccountController {

	private final SavingAccountService savingAccountService;

	@PostMapping()
	public void createSavingAccount(@RequestBody @Valid SavingAccountRequestDto requestDto){
		savingAccountService.createSavingAccount(requestDto);
	}

	@PostMapping("/charge")
	public void chargeFromMainAccount(@RequestBody @Valid ChargeSavingAccountRequestDto requestDto){
		savingAccountService.chargeFromMainAccount(requestDto);
	}
}
