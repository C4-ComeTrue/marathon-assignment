package org.c4marathon.assignment.account.presentation;

import org.c4marathon.assignment.ControllerTestSupport;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        long invalidMoney = -500L;

        mockMvc.perform(
                        post("/api/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("money", String.valueOf(invalidMoney))
                                .session(session)
                )
                .andExpect(status().isBadRequest());
    }
}