package org.c4marathon.assignment.account.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.dto.SendToSavingAccountRequest;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.annotation.Login;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final DepositService depositService;

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

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
        @Login SessionMemberInfo loginMember,
        @Valid @RequestBody WithdrawRequest request
    ) {
        accountService.withdraw(loginMember.accountId(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(
        @Login SessionMemberInfo loginMember,
        @RequestParam @NotNull @Positive Long transactionalId
    ) {
        depositService.depositByReceiver(loginMember.accountId(), transactionalId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel/withdraw")
    public ResponseEntity<Void> cancelWithdraw(
        @Login SessionMemberInfo loginMember,
        @RequestParam @NotNull @Positive Long transactionalId
    ) {
        accountService.cancelWithdraw(loginMember.accountId(), transactionalId);
        return ResponseEntity.ok().build();
    }
}
