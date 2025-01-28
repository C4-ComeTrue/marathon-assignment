package org.c4marathon.assignment.account.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.dto.SendToSavingAccountRequest;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/charge")
    public ResponseEntity<Void> charge(
            @Login SessionMemberInfo loginMember,
            @RequestParam @PositiveOrZero(message = "음수는 송금할 수 없습니다.") long money
    ) {
        accountService.chargeMoney(loginMember.accountId(), money);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/saving-account")
    public ResponseEntity<Void> sendToSavingAccount(
            @Login SessionMemberInfo loginMember,
            @Valid @RequestBody SendToSavingAccountRequest request
    ) {
        accountService.sendToSavingAccount(
                loginMember.accountId(),
                request.savingAccountId(),
                request.money()
        );
        return ResponseEntity.ok().build();
    }
}
