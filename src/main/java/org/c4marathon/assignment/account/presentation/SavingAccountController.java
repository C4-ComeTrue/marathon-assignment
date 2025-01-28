package org.c4marathon.assignment.account.presentation;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.dto.SavingAccountCreateResponse;
import org.c4marathon.assignment.account.service.SavingAccountService;
import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SavingAccountController {
    private final SavingAccountService savingAccountService;

    @PostMapping("/saving-account")
    public ResponseEntity<SavingAccountCreateResponse> createSavingAccount(@Login SessionMemberInfo loginMember) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savingAccountService.createSavingAccount(loginMember.memberId()));
    }
}
