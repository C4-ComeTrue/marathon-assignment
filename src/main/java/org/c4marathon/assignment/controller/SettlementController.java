package org.c4marathon.assignment.controller;

import java.util.List;

import org.c4marathon.assignment.common.dto.SuccessNonDataResponse;
import org.c4marathon.assignment.common.exception.enums.SuccessCode;
import org.c4marathon.assignment.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.service.SettlementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settle")
@RequiredArgsConstructor
public class SettlementController {
	private final SettlementService settleService;

	@PostMapping("/request")
	public SuccessNonDataResponse requestSettlement(@RequestBody @Valid SettlementRequestDto requestDto) {
		settleService.divideMoney(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.REQUEST_SETTLEMENT_SUCCESS);
	}

	@PostMapping("/remittance")
	public SuccessNonDataResponse remittanceSettlement(@RequestBody @Valid RemittanceRequestDto requestDto) {
		settleService.remittanceMoney(requestDto);
		return SuccessNonDataResponse.success(SuccessCode.REMITTANCE_SETTLEMENT_SUCCESS);
	}

}
