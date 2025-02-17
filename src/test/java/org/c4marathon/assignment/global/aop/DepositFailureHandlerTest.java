package org.c4marathon.assignment.global.aop;

import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.mockito.BDDMockito.*;

import org.aspectj.lang.JoinPoint;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositFailureHandlerTest {

	@Mock
	private AccountService accountService;

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private DepositFailureHandler depositFailureHandler;

	@DisplayName("successDeposit()에서 예외 발생 시 상태를 FAILED_DEPOSIT으로 변경하고 저장한다.")
	@Test
	void handleDepositFailure_ShouldUpdateStatusToFailedDeposit() {
		// given
		Transaction transactional = mock(Transaction.class);
		JoinPoint joinPoint = mock(JoinPoint.class);

		given(joinPoint.getArgs()).willReturn(new Object[]{transactional});

		// when
		depositFailureHandler.handleDepositFailure(joinPoint, new RuntimeException("Deposit failed"));

		// then
		verify(transactional).updateStatus(FAILED_DEPOSIT);
		verify(transactionRepository, times(1)).save(transactional);
	}

	@DisplayName("failedDeposit()에서 예외 발생 시 상태를 CANCEL로 변경하고 송금 취소한다.")
	@Test
	void handleFailedDepositFailure_ShouldUpdateStatusToCancelAndRollbackWithdraw() {
		// given
		Transaction transactional = mock(Transaction.class);
		given(transactional.getSenderAccountId()).willReturn(100L);
		given(transactional.getAmount()).willReturn(5000L);

		JoinPoint joinPoint = mock(JoinPoint.class);

		given(joinPoint.getArgs()).willReturn(new Object[]{transactional});

		// when
		depositFailureHandler.handleFailedDepositFailure(joinPoint, new RuntimeException("예외 발생"));

		// then
		verify(transactional).updateStatus(CANCEL);
		verify(transactionRepository, times(1)).save(transactional);
		verify(accountService, times(1)).rollbackWithdraw(100L, 5000L);
	}

}