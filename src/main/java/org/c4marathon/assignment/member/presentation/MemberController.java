package org.c4marathon.assignment.member.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.member.dto.MemberLoginRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterResponse;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberRegisterResponse> register(@Valid @RequestBody MemberRegisterRequest registerRequest) {
        MemberRegisterResponse member = memberService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody MemberLoginRequest loginRequest, HttpServletRequest request) {
        SessionMemberInfo member = memberService.login(loginRequest);
        HttpSession session = request.getSession();

        session.setAttribute(SessionConst.LOGIN_MEMBER, member);

        return ResponseEntity.ok().build();
    }
}
