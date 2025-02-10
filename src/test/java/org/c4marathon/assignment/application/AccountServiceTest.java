package org.c4marathon.assignment.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServiceTest {
	static final int TEST_USERS = 100;

	@Autowired
	AccountService accountService;
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	UserRepository userRepository;

	@AfterEach
	void tearDown() {
		userRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("모든 계좌의 돈이 넉넉하고 충전 한도를 넘지 않는 선에서 임의의 100명이 동시에 1명에게 송금할 수 있다.")
	void success_transfer_100_to_1_given_normal() throws Exception {
		// given
		List<Account> testAccounts = IntStream.range(0, TEST_USERS).mapToObj(i -> {
			User user = User.builder().email("test" + i).build();
			User savedUser = userRepository.save(user);

			Account account = Account.builder()
				.accountNumber(String.valueOf(i))
				.accountType(AccountType.CHECKING)
				.balance(i * 100L)
				.isMain(false)
				.userId(savedUser.getId())
				.build();

			return accountRepository.save(account);
		}).toList();

		long sendMoney = 100L;

		// when
		CountDownLatch countDownLatch = new CountDownLatch(TEST_USERS - 1);
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		long start = System.currentTimeMillis();
		IntStream.range(1, TEST_USERS).forEach(idx ->
			threadPool.execute(() -> {
				try {
					accountService.transfer(
						testAccounts.get(idx).getAccountNumber(),
						testAccounts.get(0).getAccountNumber(),
						sendMoney);
				} finally {
					countDownLatch.countDown();
				}
			})
		);

		countDownLatch.await(5000L, TimeUnit.MILLISECONDS);
		System.out.println("time: " + (System.currentTimeMillis() - start) + "ms");
		// then
		Account account = accountRepository.findById(testAccounts.get(0).getId()).get();
		assertThat(account.getBalance()).isEqualTo(sendMoney * (TEST_USERS - 1));

	}

	@Test
	@DisplayName("일부 예외가 일어남에도 임의의 100명이 동시에 1명에게 송금할 수 있다.")
	void success_transfer_100_to_1_given_error() throws Exception {
		// given
		List<Account> testAccounts = IntStream.range(0, TEST_USERS).mapToObj(i -> {
			User user = User.builder().email("test" + i).accCharge((i & 1) == 1 ? Integer.MAX_VALUE : 0).build(); // 일부 계좌 충전 불가
			User savedUser = userRepository.save(user);

			Account account = Account.builder()
				.accountNumber(String.valueOf(i))
				.accountType(AccountType.CHECKING)
				.balance(0L)
				.isMain(false)
				.userId(savedUser.getId())
				.build();

			return accountRepository.save(account);
		}).toList();

		long sendMoney = 100000L;

		// when
		AtomicInteger exceptionCount = new AtomicInteger();
		CountDownLatch countDownLatch = new CountDownLatch(TEST_USERS - 1);
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		long start = System.currentTimeMillis();
		IntStream.range(1, TEST_USERS).forEach(idx ->
			threadPool.execute(() -> {
				try {
					accountService.transfer(
						testAccounts.get(idx).getAccountNumber(),
						testAccounts.get(0).getAccountNumber(),
						sendMoney);
				} catch (Exception e) {
					exceptionCount.getAndIncrement();
				}finally {
					countDownLatch.countDown();
				}
			})
		);

		countDownLatch.await(5000L, TimeUnit.MILLISECONDS);
		System.out.println("time: " + (System.currentTimeMillis() - start) + "ms");

		// then
		Account account = accountRepository.findById(testAccounts.get(0).getId()).get();
		assertThat(account.getBalance()).isEqualTo(3000000L); // 충전 한도 도달
		assertThat(exceptionCount.get()).isEqualTo(TEST_USERS - 31);
	}
}
