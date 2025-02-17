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

class SavingAccountControllerTest extends ControllerTestSupport {
    @BeforeEach
    void initSession() {
        session = new MockHttpSession();
        SessionMemberInfo memberInfo = new SessionMemberInfo(1L, "test@test.com", 1L);
        session.setAttribute(SessionConst.LOGIN_MEMBER, memberInfo);
    }

    @DisplayName("적금 계좌를 생성한다.")
    @Test
    void createSavingAccount() throws Exception {
        // when // then
        mockMvc.perform(
                        post("/api/saving-account")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                )
                .andExpect(status().isCreated());
    }
}