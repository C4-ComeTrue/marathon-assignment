package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.dto.SuccessNonDataResponse;
import org.c4marathon.assignment.common.exception.enums.SuccessCode;
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
	public SuccessNonDataResponse createSavingAccount(@RequestBody @Valid SavingAccountRequestDto requestDto){
		savingAccountService.createSavingAccount(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.CREATE_SAVING_ACCOUNT_SUCCESS);
	}

	@PostMapping("/charge")
	public SuccessNonDataResponse chargeFromMainAccount(@RequestBody @Valid ChargeSavingAccountRequestDto requestDto){
		savingAccountService.chargeFromMainAccount(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.TRANSFER_SAVING_ACCOUNT_SUCCESS);
	}
}
