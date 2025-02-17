package org.c4marathon.assignment.global.aop;

import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DepositFailureHandler {
	private final AccountService accountService;
	private final TransactionRepository transactionRepository;

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.successDeposit(..))", throwing = "ex")
	// @Retryable()
	public void handleDepositFailure(JoinPoint joinPoint, Exception ex) {

		Object[] args = joinPoint.getArgs();
		Transaction transactional = (Transaction) args[0];

		transactional.updateStatus(FAILED_DEPOSIT);
		transactionRepository.save(transactional);
	}

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.failedDeposit(..))", throwing = "ex")
	// @Retryable()
	public void handleFailedDepositFailure(JoinPoint joinPoint, Exception ex) {
		Object[] args = joinPoint.getArgs();
		Transaction transactional = (Transaction)args[0];

		transactional.updateStatus(CANCEL);
		transactionRepository.save(transactional);

		accountService.rollbackWithdraw(transactional.getSenderAccountId(), transactional.getAmount());
	}
}
