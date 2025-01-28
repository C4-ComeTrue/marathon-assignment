package org.c4marathon.assignment.member.presentation;

import org.c4marathon.assignment.ControllerTestSupport;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.dto.MemberLoginRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterRequest;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTest extends ControllerTestSupport {

    @DisplayName("회원가입 성공")
    @Test
    void register() throws Exception {
        // given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest("test@test.com", "테스트", "testPassword!");

        // when // then
        mockMvc.perform(
                post("/api/register")
                                .content(objectMapper.writeValueAsString(registerRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated());
    }
    @DisplayName("로그인 성공")
    @Test
    void login() throws Exception {
        // given
        MemberLoginRequest loginRequest = new MemberLoginRequest("test@test.com", "testPassword!");

        Member member = Member.create("test@test.com","테스트", "testPassword!");
        given(memberService.login(any())).willReturn(new SessionMemberInfo(member.getId(), member.getEmail(), 1L));

        // when // then
        mockMvc.perform(
                        post("/api/login")
                                .content(objectMapper.writeValueAsString(loginRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute(SessionConst.LOGIN_MEMBER, new SessionMemberInfo(member.getId(), member.getEmail(), 1L)));
    }
}