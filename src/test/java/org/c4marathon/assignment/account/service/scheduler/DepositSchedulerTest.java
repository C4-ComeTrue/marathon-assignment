package org.c4marathon.assignment.account.service.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.transactional.domain.TransactionalStatus.*;
import static org.c4marathon.assignment.transactional.domain.TransactionalType.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.service.TransactionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DepositSchedulerTest {

	@Mock
	private DepositService depositService;

	@Mock
	private TransactionalService transactionalService;

	@InjectMocks
	private DepositScheduler depositScheduler;

	private MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

	public static final int PAGE_SIZE = 100;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(depositScheduler, "threadPoolExecutor", threadPoolExecutor);
	}

	@DisplayName("ThreadPool이 초기화되고 모든 태스크가 병렬로 처리되는지 검증")
	@Test
	void depositsParallelExecution() throws Exception {
	    // given
		int numberOfDeposits = 16;
		CountDownLatch processLatch = new CountDownLatch(numberOfDeposits);
		AtomicInteger concurrentExecutions = new AtomicInteger(0);
		AtomicInteger maxConcurrentExecutions = new AtomicInteger(0);

		List<TransferTransactional> transactionals = new ArrayList<>();
		for (int i = 0; i < numberOfDeposits; i++) {
			TransferTransactional transactional = TransferTransactional.create(
				i + 1L,
				i + 2L,
				1000L,
				IMMEDIATE_TRANSFER,
				WITHDRAW,
				LocalDateTime.now()
			);
			transactionals.add(transactional);
		}

		given(transactionalService.findTransactionalByStatusWithLastId(
			eq(WITHDRAW), isNull(), eq(PAGE_SIZE)))
			.willReturn(transactionals)
			.willReturn(Collections.emptyList());


		doAnswer(invocation -> {
			int concurrent = concurrentExecutions.incrementAndGet();
			maxConcurrentExecutions.updateAndGet(max -> Math.max(max, concurrent));
			try {
				// 실제 작업 시뮬레이션
				Thread.sleep(50);
			} finally {
				concurrentExecutions.decrementAndGet();
				processLatch.countDown();
			}
			return null;
		}).when(depositService).successDeposit(any(TransferTransactional.class));

	    // when
		long startTime = System.currentTimeMillis();
		depositScheduler.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// then
		long executionTime = System.currentTimeMillis() - startTime;
		assertThat(allProcessed).isTrue();
		assertThat(maxConcurrentExecutions.get()).isEqualTo(8);
		assertThat(executionTime).isLessThan(400L);

		verify(depositService, times(numberOfDeposits)).successDeposit(any(TransferTransactional.class));
		verify(transactionalService, times(2))
			.findTransactionalByStatusWithLastId(any(), any(), eq(PAGE_SIZE));
	}

	@DisplayName("같은 계좌에 동시에 입금 요청이 와도 동시성 문제 없이 정확한 금액이 입금된다.")
	@Test
	void depositsConcurrency() throws Exception {
	    // given
		int numberOfDeposits = 100;
		CountDownLatch processLatch = new CountDownLatch(numberOfDeposits);
		long expectedTotalAmount = IntStream.range(0, numberOfDeposits)
			.mapToLong(i -> 100L * i)
			.sum();

		AtomicLong actualTotalAmount = new AtomicLong(0);

		List<TransferTransactional> transactionals = IntStream.range(0, numberOfDeposits)
			.mapToObj(i -> TransferTransactional.create(
				i + 1L,
				i + 2L,
				100L * i,
				IMMEDIATE_TRANSFER,
				WITHDRAW,
				LocalDateTime.now()
			))
			.toList();

		given(transactionalService.findTransactionalByStatusWithLastId(
			eq(WITHDRAW), isNull(), eq(PAGE_SIZE)))
			.willReturn(transactionals)
			.willReturn(Collections.emptyList());

		doAnswer(invocation -> {
			TransferTransactional transactional = invocation.getArgument(0);
			actualTotalAmount.addAndGet(transactional.getAmount());
			Thread.sleep(10);
			processLatch.countDown();
			return null;
		}).when(depositService).successDeposit(any(TransferTransactional.class));

		// when
		depositScheduler.deposits();
		boolean allProcessed = processLatch.await(5, TimeUnit.SECONDS);

		// then
		assertThat(allProcessed).isTrue();
		assertThat(actualTotalAmount.get()).isEqualTo(expectedTotalAmount);
		verify(depositService, times(numberOfDeposits)).successDeposit(any(TransferTransactional.class));

	}

	@DisplayName("입금 재시도 스케줄러가 실패한 트랜잭션을 조회하고 다시 입금 시도를 수행한다.")
	@Test
	void retryDeposit() {
		// given
		int numberOfDeposits = 10;
		List<TransferTransactional> transactionals = IntStream.range(0, numberOfDeposits)
			.mapToObj(i -> TransferTransactional.create(
				i + 1L,
				i + 2L,
				100L * i,
				IMMEDIATE_TRANSFER,
				FAILED_DEPOSIT,
				LocalDateTime.now()
			))
			.toList();


		given(transactionalService.findTransactionalByStatusWithLastId(
			eq(FAILED_DEPOSIT), isNull(), eq(PAGE_SIZE)))
			.willReturn(transactionals)
			.willReturn(Collections.emptyList());

		// when
		depositScheduler.retryDeposit();

		// then
		verify(transactionalService, times(2))
			.findTransactionalByStatusWithLastId(eq(FAILED_DEPOSIT), any(), eq(PAGE_SIZE));

		verify(depositService, times(numberOfDeposits)).failedDeposit(any(TransferTransactional.class));

	}
}
