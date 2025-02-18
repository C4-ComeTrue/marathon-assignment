package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.common.dto.SuccessNonDataResponse;
import org.c4marathon.assignment.common.exception.enums.SuccessCode;
import org.c4marathon.assignment.dto.request.ChargeMainAccountRequestDto;
import org.c4marathon.assignment.dto.request.TransferRequestDto;
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
	public SuccessNonDataResponse chargeMainAccount(@RequestBody @Valid ChargeMainAccountRequestDto requestDto){
		mainAccountService.chargeMainAccount(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.CHARGE_MAIN_ACCOUNT_SUCCESS);
	}

	@PostMapping("/transfer")
	public SuccessNonDataResponse transferToOtherAccount(@RequestBody @Valid TransferRequestDto requestDto){
		mainAccountService.transferToOtherAccount(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.TRANSFER_SAVING_ACCOUNT_SUCCESS);
	}

}
