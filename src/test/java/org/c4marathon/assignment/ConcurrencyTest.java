package org.c4marathon.assignment;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.MemberService;
import org.c4marathon.assignment.service.SavingsAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConcurrencyTest {

	@Autowired SavingsAccountService savingsAccountService;

	@Autowired AccountService accountService;

	@Autowired MemberService memberService;

	@Autowired AccountRepository accountRepository;

	@Autowired SavingsAccountRepository savingsAccountRepository;

	@Test
	void 메인_계좌_충전_및_적금_자동_이체_동시성_테스트() throws InterruptedException {
		// given
		// 1. 회원 가입 -> 메인 계좌 자동 생성
		var response = memberService.register("email", "password");
		var memberId = response.memberId();
		var accountId = response.accountId();
		var concurrentUser = 1;
		var withdrawAmount = 5000;
		var chargeAmount = 10000;

		// 2. 적금 계좌 생성
		var res = savingsAccountService.createSavingsAccount(memberId, "name", withdrawAmount, SavingsType.REGULAR);
		var savingAccountId = res.id();

		var executorService = Executors.newFixedThreadPool(1000);
		var countDownLatch = new CountDownLatch(concurrentUser);

		// when
		// 정기 적금 이체 자체는 동시에 수행되는 경우가 없지만, 충전 로직과 동시에 일어나는 경우를 테스트
		for (int i = 0; i < concurrentUser; i++) {
			executorService.execute(() -> {
				accountService.charge(accountId, chargeAmount);              // 메인 계좌 5번 10000원 충전
				savingsAccountService.transferForRegularSavings(memberId);   // 메인 계좌 5번 5000원이 감소, 적금 계좌 5번 5000원 증가
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		executorService.shutdown();

		// then
		var accountEntity = accountRepository.findById(accountId).orElseThrow();
		var savingsAccountEntity = savingsAccountRepository.findById(savingAccountId).orElseThrow();
		assertThat(accountEntity.getAmount()).isEqualTo(chargeAmount * concurrentUser - withdrawAmount * concurrentUser);
		assertThat(savingsAccountEntity.getAmount()).isEqualTo(withdrawAmount * concurrentUser);
	}
}
