package org.c4marathon.assignment.account.presentation;

import static org.c4marathon.assignment.transaction.domain.TransactionType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.ControllerTestSupport;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

class AccountControllerTest extends ControllerTestSupport {

    @BeforeEach
    void initSession() {
        session = new MockHttpSession();
        SessionMemberInfo memberInfo = new SessionMemberInfo(1L, "test@test.com", 1L);
        session.setAttribute(SessionConst.LOGIN_MEMBER, memberInfo);
    }

    @DisplayName("메인 계좌 충전 성공")
    @Test
    void chargeMoney() throws Exception {
        // given
        long chargeMoney = 50_000L;

        // when // then
        mockMvc.perform(
                post("/api/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("money", String.valueOf(chargeMoney))
                        .session(session)
                )
                .andExpect(status().isOk());
    }


    @DisplayName("money가 음수일 경우 예외가 발생한다.")
    @Test
    void chargeWithInvalidMoney() throws Exception {
        // given
        long invalidMoney = -500L;

        // when // then
        mockMvc.perform(
                        post("/api/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("money", String.valueOf(invalidMoney))
                                .session(session)
                )
                .andExpect(status().isBadRequest());
    }

    @DisplayName("다른 메인 계좌로 송금 성공")
    @Test
    void withdraw() throws Exception {
        // given
        WithdrawRequest request = new WithdrawRequest(1L, 5000L, IMMEDIATE_TRANSFER);

        // when // then
        mockMvc.perform(
                post("/api/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session)
            )
            .andExpect(status().isOk());
    }

    @DisplayName("출금 금액이 음수일 경우 예외가 발생한다.")
    @Test
    void withdrawWithNegativeAmount() throws Exception {
        // given
        WithdrawRequest request = new WithdrawRequest(2L, -5000L, IMMEDIATE_TRANSFER); // 음수 금액

        // when // then
        mockMvc.perform(
                post("/api/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session)
            )
            .andExpect(status().isBadRequest());
    }

}