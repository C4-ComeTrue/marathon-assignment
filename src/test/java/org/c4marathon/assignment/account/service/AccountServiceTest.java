package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.util.Const.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;
import static org.c4marathon.assignment.transaction.domain.TransactionStatus.PENDING_DEPOSIT;
import static org.c4marathon.assignment.transaction.domain.TransactionType.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.event.transactional.TransactionCreateEvent;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.repository.TransactionRepository;
import org.c4marathon.assignment.transaction.exception.InvalidTransactionStatusException;
import org.c4marathon.assignment.transaction.exception.NotFoundTransactionException;
import org.c4marathon.assignment.transaction.exception.UnauthorizedTransactionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends IntegrationTestSupport {
	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private SavingAccountRepository savingAccountRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@MockBean
	private ApplicationEventPublisher eventPublisher;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(accountService, "eventPublisher", eventPublisher);
	}

	@AfterEach
	void tearDown() {
		accountRepository.deleteAllInBatch();
		savingAccountRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();

	}

	@DisplayName("메인 계좌를 생성한다.")
	@Test
	void createAccount() {
		// given
		Member member = createMember();

		// when
		accountService.createAccount(member.getId());

		// then
		Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
		assertThat(updatedMember.getAccountId()).isNotNull();

		Account account = accountRepository.findById(updatedMember.getAccountId()).orElseThrow();
		assertThat(account).isNotNull();
	}

	@DisplayName("메인 계좌에 돈을 충전한다.")
	@Test
	void chargeMoney() {
		// given
		Account account = createAccount(DEFAULT_BALANCE);

		long chargeAmount = 50_000L;

		// when
		accountService.chargeMoney(account.getId(), chargeAmount);

		// then
		Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
		assertThat(updatedAccount.getMoney()).isEqualTo(50_000L);
		assertThat(updatedAccount.getChargeLimit()).isEqualTo(2_950_000L);
	}

	@DisplayName("일일 충전 한도를 넘어가는 금액을 충전 시도할 경우 예외가 발생한다.")
	@Test
	void chargeMoneyWithDailyLimitExceeded() {
		// given
		Account account = createAccount(DEFAULT_BALANCE);

		long chargeAmount = 3_500_000L;

		// when // then
		assertThatThrownBy(() -> accountService.chargeMoney(account.getId(), chargeAmount))
			.isInstanceOf(DailyChargeLimitExceededException.class);
	}

	@DisplayName("메인 계좌에서 적금 계좌로 송금한다.")
	@Test
	void sendToSavingAccount() {
		// given
		Member member = createMember();

		Account account = Account.create(10000L);
		member.setMainAccountId(account.getId());
		accountRepository.save(account);

		SavingAccount savingAccount = SavingAccount.create(1000L, member);
		savingAccountRepository.save(savingAccount);

		long sendMoney = 5_000L;

		// when
		accountService.sendToSavingAccount(account.getId(), savingAccount.getId(), sendMoney);

		// then
		Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
		SavingAccount updatedSavingAccount = savingAccountRepository.findById(savingAccount.getId()).orElseThrow();

		assertThat(updatedAccount.getMoney()).isEqualTo(5_000L);
		assertThat(updatedSavingAccount.getBalance()).isEqualTo(6_000L);
	}

	@DisplayName("송금 시도할 때 메인 계좌 잔액이 부족하면 10,000원 단위로 충전 후 송금 한다.")
	@Test
	void sendToSavingAccountWithInsufficientBalance() {
		// given
		Member member = createMember();

		Account account = Account.create(12000L);
		member.setMainAccountId(account.getId());
		accountRepository.save(account);

		SavingAccount savingAccount = SavingAccount.create(1000L, member);
		savingAccountRepository.save(savingAccount);

		long sendMoney = 20_000L;

		// when
		accountService.sendToSavingAccount(account.getId(), savingAccount.getId(), sendMoney);

		// then
		Account updatedAccount = accountRepository.findById(account.getId())
			.orElseThrow(NotFoundAccountException::new);
		SavingAccount updatedSavingAccount = savingAccountRepository.findById(savingAccount.getId())
			.orElseThrow(NotFoundAccountException::new);

		assertThat(updatedAccount.getMoney()).isEqualTo(2000L);
		assertThat(updatedSavingAccount.getBalance()).isEqualTo(21000L);

	}

	@Transactional
	@DisplayName("송금 시 메인 계좌에서 출금하고 TransferTransactional 생성 이벤트를 발행한다.")
	@Test
	void withdraw() {
		// given
		Account senderAccount = createAccount(50000L);
		WithdrawRequest request = new WithdrawRequest(2L, 20000L, IMMEDIATE_TRANSFER);

		// when
		accountService.withdraw(senderAccount.getId(), request);

		// then
		Account updatedSenderAccount = accountRepository.findById(senderAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		assertThat(updatedSenderAccount.getMoney()).isEqualTo(30000L);

		verify(eventPublisher, times(1)).publishEvent(any(TransactionCreateEvent.class));

	}

	@Transactional
	@DisplayName("송금 시 메인 계좌에 잔액이 부족하면 충전을 하고 출금한다.")
	@Test
	void withdrawWithInsufficientBalance() {
		// given
		Account senderAccount = createAccount(50000L);
		WithdrawRequest request = new WithdrawRequest(2L, 200000L, IMMEDIATE_TRANSFER);

		// when
		accountService.withdraw(senderAccount.getId(), request);

		// then
		Account updatedSenderAccount = accountRepository.findById(senderAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		assertThat(updatedSenderAccount.getMoney()).isZero();

		verify(eventPublisher, times(1)).publishEvent(any(TransactionCreateEvent.class));

	}

	@Transactional
	@DisplayName("송금 시 잔액이 부족해 충전할 때 일일 한도를 초과하면 예외가 발생한다.")
	@Test
	void withdrawWithDailyChargeLimit() {
		// given
		Account senderAccount = createAccount(5000L);
		WithdrawRequest request = new WithdrawRequest(2L, 3_500_000L, IMMEDIATE_TRANSFER);

		// when // then
		Long senderAccountId = senderAccount.getId();
		assertThatThrownBy(() -> accountService.withdraw(senderAccountId, request))
			.isInstanceOf(DailyChargeLimitExceededException.class);
	}

	@Transactional
	@DisplayName("수령 받지 않은 송금일 경우 송금한 사용자가 송금 취소를 할 수 있다.")
	@Test
	void cancelWithdraw() {
		// given
		Account senderAccount = createAccount(5000L);
		Account receiverAccount = createAccount(1000L);
		LocalDateTime sendTime = LocalDateTime.now();

		Transaction transaction = Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L,
			PENDING_TRANSFER, PENDING_DEPOSIT, sendTime);
		transactionRepository.save(transaction);
		// when
		accountService.cancelWithdraw(senderAccount.getId(), transaction.getId());

		// then
		Account updatedSenderAccount = accountRepository.findById(senderAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		Transaction updatedTransaction = transactionRepository.findById(transaction.getId())
			.orElseThrow(NotFoundTransactionException::new);

		assertThat(updatedSenderAccount.getMoney()).isEqualTo(6000L);
		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(1000L);
		assertThat(updatedTransaction.getStatus()).isEqualTo(CANCEL);
	}

	@DisplayName("이미 수령 받은 송금을 취소하려고 하면 예외가 발생한다.")
	@Test
	void cancelWithdrawForSuccessDeposit() {
		// given
		Account senderAccount = createAccount(5000L);
		Account receiverAccount = createAccount(1000L);
		LocalDateTime sendTime = LocalDateTime.now();

		Transaction transaction = Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L,
			PENDING_TRANSFER, SUCCESS_DEPOSIT, sendTime);
		transactionRepository.save(transaction);

		// when // then
		assertThatThrownBy(() -> accountService.cancelWithdraw(senderAccount.getId(), transaction.getId()))
			.isInstanceOf(InvalidTransactionStatusException.class);
	}

	@DisplayName("송금을 한 사용자가 아닌 사용자가 취소하려고 하면 예외가 발생한다.")
	@Test
	void cancelWithdrawByUnauthorizedMember() {
		// given
		Account senderAccount = createAccount(5000L);
		Account receiverAccount = createAccount(1000L);
		Account otherAccount = createAccount(5000L);
		LocalDateTime sendTime = LocalDateTime.now();

		Transaction transaction = Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L,
			PENDING_TRANSFER, PENDING_DEPOSIT, sendTime);
		transactionRepository.save(transaction);

		// when // then
		assertThatThrownBy(() -> accountService.cancelWithdraw(otherAccount.getId(), transaction.getId()))
			.isInstanceOf(UnauthorizedTransactionException.class);
	}

	@Transactional
	@DisplayName("72시간이 지난 수령 대기(PENDING_DEPOSIT) 중인 송금 내역은 취소시킨다.")
	@Test
	void cancelWithdrawByExpirationTime() throws Exception {
		// given
		Account senderAccount = createAccount(5000L);
		Account receiverAccount = createAccount(1000L);
		LocalDateTime sendTime = LocalDateTime.now().minusHours(73);

		Transaction transaction = Transaction.create(senderAccount.getId(), receiverAccount.getId(), 1000L,
			PENDING_TRANSFER, PENDING_DEPOSIT, sendTime);
		transactionRepository.save(transaction);

		// when
		accountService.cancelWithdrawByExpirationTime(transaction);

		// then
		Account updatedSenderAccount = accountRepository.findById(senderAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		Account updatedReceiverAccount = accountRepository.findById(receiverAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		Transaction updatedTransaction = transactionRepository.findById(transaction.getId())
			.orElseThrow(NotFoundTransactionException::new);

		assertThat(updatedSenderAccount.getMoney()).isEqualTo(6000L);
		assertThat(updatedReceiverAccount.getMoney()).isEqualTo(1000L);
		assertThat(updatedTransaction.getStatus()).isEqualTo(CANCEL);
	}

	@DisplayName("입금 재시도가 실패하면 송금 롤백이 성공한다.")
	@Test
	void rollbackWithdraw() {
		// given
		Account senderAccount = createAccount(10000L);
		long rollbackMoney = 20000L;

		// when
		accountService.rollbackWithdraw(senderAccount.getId(), rollbackMoney);

		// then
		Account updatedSenderAccount = accountRepository.findById(senderAccount.getId())
			.orElseThrow(NotFoundAccountException::new);
		assertThat(updatedSenderAccount.getMoney()).isEqualTo(30000L);
	}

	private Account createAccount(long money) {
		Account senderAccount = Account.create(money);
		accountRepository.save(senderAccount);
		return senderAccount;
	}

	private Member createMember() {
		Member member = Member.create("test@test.com", "테스트", "testPassword");
		memberRepository.save(member);
		return member;
	}

}