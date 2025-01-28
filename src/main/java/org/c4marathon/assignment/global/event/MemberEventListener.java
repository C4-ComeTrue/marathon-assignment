package org.c4marathon.assignment.global.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberEventListener {

    private final AccountService accountService;
    @EventListener
    public void handleMemberRegisteredEvent(MemberRegisteredEvent event) {
        accountService.createAccount(event.memberId());
    }
}
