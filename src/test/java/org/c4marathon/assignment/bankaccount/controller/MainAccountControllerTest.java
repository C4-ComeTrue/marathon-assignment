package org.c4marathon.assignment.bankaccount.controller;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.bankaccount.dto.request.SendToSavingRequestDto;
import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.limit.LimitConst;
import org.c4marathon.assignment.bankaccount.service.MainAccountService;
import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.common.session.SessionConst;
import org.c4marathon.assignment.common.session.SessionMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainAccountController.class)
class MainAccountControllerTest {

	private final String REQUEST_URL = "/api/accounts/main";

	@Autowired
	MockMvc mockMvc;

	@MockBean
	private MainAccountService mainAccountService;

	@Autowired
	private ObjectMapper objectMapper;

	private MockHttpSession session;

	@BeforeEach
	void initSession() {
		session = new MockHttpSession();
		SessionMemberInfo memberInfo = SessionMemberInfo.builder()
			.memberPk(1L)
			.memberId("testId")
			.mainAccountPk(1L)
			.build();
		session.setAttribute(SessionConst.MEMBER_INFO, memberInfo);
	}

	@Nested
	@DisplayName("메인 계좌 충전 테스트")
	class Charge {
		private final int money = 1000;
		private int baseMoney = 0;

		@Test
		@DisplayName("유효한 요청이면 정상적으로 메인 계좌에 돈을 충전한다.")
		void request_with_a_valid_form() throws Exception {
			// Given
			SessionMemberInfo memberInfo = (SessionMemberInfo)session.getAttribute(SessionConst.MEMBER_INFO);
			long mainAccountPk = memberInfo.mainAccountPk();

			given(mainAccountService.chargeMoney(mainAccountPk, money)).willReturn(baseMoney + money);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/charge/{money}", money).session(session));

			// Then
			resultActions.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString()
				.equals(baseMoney + money);
		}

		@Test
		@DisplayName("충전 금액이 음수면 ConstraintViolationException 예외가 발생한다.")
		void request_with_minus_money() throws Exception {
			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/charge/{money}", -1).session(session));

			// Then
			resultActions.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_ARGUMENT_ERROR.getMessage()));
		}
	}

	@Nested
	@DisplayName("메인 계좌에서 적금 계좌 이체 테스트")
	class SendToSavingAccount {

		@Test
		@DisplayName("존재하는 계좌 정보와 1원 이상의 금액으로 요청하면 이체를 성공한다.")
		void request_with_valid_account_and_valid_money() throws Exception {
			// Given
			SendToSavingRequestDto requestDto = new SendToSavingRequestDto(1, 1000);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/send/saving").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isOk());
		}

		@Test
		@DisplayName("존재하지 않는 계좌 정보(0 이하의 pk)로 요청하면 실패한다.")
		void request_with_non_valid_account() throws Exception {
			// Given
			SendToSavingRequestDto requestDto = new SendToSavingRequestDto(0, 1000);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/send/saving").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("이체 금액을 0 이하로 요청하면 실패한다.")
		void request_with_non_valid_money() throws Exception {
			// Given
			SendToSavingRequestDto requestDto = new SendToSavingRequestDto(1, 0);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/send/saving").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("메인 계좌 조회 테스트")
	class GetMainAccountInfo {

		@Test
		@DisplayName("로그인한 사용자는 메인 계좌 조회에 성공한다")
		void request_with_login_member() throws Exception {
			// Given
			MainAccountResponseDto responseDto = MainAccountResponseDto.builder()
				.accountPk(1L)
				.chargeLimit(LimitConst.CHARGE_LIMIT)
				.money(0)
				.build();
			given(mainAccountService.getMainAccountInfo(anyLong())).willReturn(responseDto);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL).session(session));

			// Then
			resultActions.andExpectAll(status().isOk(), jsonPath("$.accountPk").value(responseDto.accountPk()),
				jsonPath("$.chargeLimit").value(responseDto.chargeLimit()),
				jsonPath("$.money").value(responseDto.money()));
		}
	}
}
