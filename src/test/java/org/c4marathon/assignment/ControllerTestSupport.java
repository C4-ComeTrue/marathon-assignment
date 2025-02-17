package org.c4marathon.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.c4marathon.assignment.account.presentation.AccountController;
import org.c4marathon.assignment.account.presentation.SavingAccountController;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.account.service.SavingAccountService;
import org.c4marathon.assignment.member.presentation.MemberController;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.settlement.presentation.SettlementController;
import org.c4marathon.assignment.settlement.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
    MemberController.class,
    AccountController.class,
    SavingAccountController.class,
    SettlementController.class
})
public class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected MemberService memberService;

    @MockBean
    protected AccountService accountService;

    @MockBean
    protected SavingAccountService savingAccountService;

    @MockBean
    protected SettlementService settlementService;

    @MockBean
    protected DepositService depositService;

    protected MockHttpSession session;

}
