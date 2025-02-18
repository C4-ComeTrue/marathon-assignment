package org.c4marathon.assignment.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.common.QueryExecuteTemplate;
import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.c4marathon.assignment.common.string.StringEnum;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementMember;
import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.enums.TransactionStatus;
import org.c4marathon.assignment.domain.enums.SettlementType;
import org.c4marathon.assignment.domain.enums.TransactionType;
import org.c4marathon.assignment.dto.request.ReceiveSettlementRequestDto;
import org.c4marathon.assignment.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.repository.SettlementMemberRepository;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.c4marathon.assignment.repository.TransactionRepository;
import org.c4marathon.assignment.repository.TransactionRepositoryCursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import io.netty.util.internal.StringUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;
	private final SettlementMemberRepository settlementMemberRepository;
	private final TransactionRepository transactionRepository;
	private final TransactionRepositoryCursor transactionRepositoryCursor;
	private final UserService userService;
	private final MainAccountService mainAccountService;
	private final JavaMailSender javaMailSender;
	private final RedisTemplate<String,Long> redisTemplate;

	private static final int MAX_RETRY_COUNT = 3;
	private static final int BATCH_SIZE = 1000;

	/**
	 * [Step3] 정산
	 * @param requestDto - requestAccountId, totalAmount, type
	 * */
	@Transactional
	public void divideMoney(SettlementRequestDto requestDto) {

		if (requestDto.type() == SettlementType.EQUAL) {
			divideEqual(requestDto);
		} else {
			divideRandom(requestDto);
		}
	}


	/**
	 * [Step4] 돈을 보냈다는 알림 보내는 함수
	 * "송금 요청이 도착했습니다. 72시간 내에 수령해주세요!"
	 * */
	private void sendNotification(long requestAccountId, String title) {
		int attempt = 0;
		boolean isSent = false;

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MainAccount mainAccount = mainAccountService.getMainAccount(requestAccountId);
		User user = userService.getUser(mainAccount.getUser().getId());

		while (attempt < MAX_RETRY_COUNT && !isSent) {
			try {
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
				mimeMessageHelper.setTo(user.getEmail());
				mimeMessageHelper.setSubject(title);
				javaMailSender.send(mimeMessage);
				isSent = true;
			} catch (Exception e) {
				attempt++;
				log.warn("메일 전송 실패 ({}회차): {}, 에러: {}", attempt, user.getEmail(), e.getMessage());

				if (attempt < MAX_RETRY_COUNT) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						log.error("메일 전송 재시도 대기 중 인터럽트 발생: {}", ie.getMessage());
					}
				} else {
					log.error("메일 전송 최종 실패: {}", user.getEmail());
				}
			}
		}
	}

	/**
	 * [Step4] 정산을 위한 송금 요청하기(대기)
	 *
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void requestRemittanceMoney(RemittanceRequestDto requestDto) {
		SettlementMember settlementMember = settlementMemberRepository.findBySettlement(requestDto.settlementId()).orElseThrow(() -> new NotFoundException(
			ErrorCode.NOT_FOUND_SETTLEMENT_MEMBER));
		Transaction transaction = new Transaction(requestDto.requestAccountId(), requestDto.settlementMemberAccountId(),settlementMember,
			requestDto.amount(), TransactionStatus.REQUESTED, requestDto.type());
		transactionRepository.save(transaction);

		if (requestDto.type() == TransactionType.REALTIME) {
			// recieveMoney(transaction);
			// ✅ 돈 받는 로직 추가 예정
		} else {
			transaction.pendingTransaction(); // 상태 변환
			transactionRepository.save(transaction);
			sendNotification(requestDto.requestAccountId(), StringEnum.PENDING_MAIL_TITLE.toString());
			//✅ 일단 내 계좌에서 돈을 빼고, 돈이 부족한지 파악하고, 충전가능하다면 충전하는 로직 기존의것 디벨롭해서 추가 예정
		}

	}

	/**
	 * [Step4] 돈을 받는 함수
	 * 전체가 다 정산되면 Settlement에 status Success로 변경
	 * @requestDto
	 * long transactionId
	 * long settlementMemberId
	 * */
	@Transactional
	public void receiveMoney(ReceiveSettlementRequestDto requestDto) {
		//(1) TransactionId를 조회해서 해당 transaction SUCCESS 상태 변환

		//(2) SettlementMemberId로 SettlementMent 조회해서 => 모든 Settlement들 조회
		//(3) Settlement에 있는 내역들이 모두 정산되었으면 SUCCESS로 상태 바꾸기 ( 부분이면 PENDING으로변경)
		//(3-1) -> 이러기 위해서는 SettlementMember에 정산 여부 상태값이 다시 추가 되어야 할것 같다.

		// queuePendingTransaction(senderId, recieverId, amount);
	}

	/**
	 * [Step4] (72시간 초과 시 자동 취소)
	 * */
	@Scheduled(fixedRate = 60000) // 1분마다 실행
	@Transactional
	public void cancelExpiredTransactions() {
		LocalDateTime expirationTime = LocalDateTime.now().minusHours(72);

		QueryExecuteTemplate.<Transaction>selectAndExecuteWithCursorAndPageLimit(
			-1,
			BATCH_SIZE,
			lastTransaction -> transactionRepositoryCursor.findExpiredTransactions(
				expirationTime,
				lastTransaction == null ? null : lastTransaction.getPendingDate(),
				lastTransaction == null ? null : lastTransaction.getId(),
				BATCH_SIZE
			),
			this::processExpiredTransactions
		);

	}

	/**
	 * [Step4] (72시간 초과 시 자동 취소) >  (환불 + 상태 변경)
	 */
	@Transactional
	public void processExpiredTransactions(List<Transaction> transactions) {
		List<Long> transactionIds = new ArrayList<>();

		for (Transaction transaction : transactions) {
			transaction.cancelTransaction();
			transactionIds.add(transaction.getId());
		}
		transactionRepository.updateExpiredTransactions(transactionIds);

		//환불 송금 요청
		for (Transaction transaction : transactions) {
			refundToSender(transaction);
		}
	}

	/**
	 * [Step4] 24시간 남은 송금 리마인드 알림
	 * 5분마다 스케줄러를 통해 24시간 남은 송금 조회
	 * */
	@Scheduled(fixedRate = 300000)
	public void sendRemindersForPendingTransactions() {
		LocalDateTime endDate = LocalDateTime.now().minusHours(48);

		QueryExecuteTemplate.<Transaction>selectAndExecuteWithCursorAndPageLimit(
			-1,
			BATCH_SIZE,
			lastTransaction -> transactionRepositoryCursor.findRemindableTransactions(
				endDate,
				lastTransaction == null ? null : lastTransaction.getPendingDate(),
				lastTransaction == null ? null : lastTransaction.getId(),
				BATCH_SIZE
			),
			this::processMailMultiThread
		);
	}

	/**
	 * 1안 : 멀티스레드+ 동기적으로 메일 전송
	 * (2안 : 메일 전송을 비동기로 처리하고 메일 전송 실패 건에 대해서만 롤백하는밥법)
	 * */
	@Transactional
	public void processMailMultiThread(List<Transaction> transactions) {
		transactions.parallelStream().forEach(transaction -> {
			sendNotification(transaction.getReceiverAccountId(), StringEnum.ALERT_24_MAIL_TITLE.toString());
		});

		updateMailSentStatus(transactions);
	}

	@Transactional
	public void updateMailSentStatus(List<Transaction> transactions) {
		List<Long> transactionIds = transactions.stream()
			.map(Transaction::getId)
			.toList();

		transactionRepository.updateMailSent(transactionIds);
	}

	/**
	 * [Step3] 1/n정산
	 * 딱 나누어 떨어지지 않을 경우 정해진 순서 없이 아무나에게 1씩 추가 정산 요청
	 * */
	private void divideEqual(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();

		long baseAmount = totalAmount / totalPeople;
		long remainAmount = totalAmount % totalPeople;

		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalPeople,
			SettlementType.EQUAL, TransactionStatus.REQUESTED, null);

		//기본 금액 전체 할당
		List<SettlementMember> updatedMembers = settlementMemberIds.stream().map(memberId -> new SettlementMember(
			memberId, baseAmount, settlement
		)).collect(Collectors.toList());

		//남은 금액 1원씩 할당
		if (remainAmount > 0) {
			Collections.shuffle(updatedMembers);
			for (int i = 0; i < remainAmount; i++) {
				SettlementMember member = updatedMembers.get(i);
				member.updateAmount(1);
			}
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	/**
	 * [Step3] Random 정산 (0원 가능)
	 * 카카오페이의 돈 뿌리기 처럼 돈을 못받는? 정산하지 않아도 되는 사람이 생길 수도 있다.
	 * */
	private void divideRandom(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();
		SecureRandom random = new SecureRandom();

		//Settlement
		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalPeople,
			SettlementType.RANDOM, TransactionStatus.REQUESTED, null);

		//랜덤으로 돈 배정
		Collections.shuffle(settlementMemberIds);
		int remainAmount = totalAmount;
		List<SettlementMember> updatedMembers = new ArrayList<>();

		for (int i = 0; i < totalPeople - 1; i++) {
			int randomAmount = random.nextInt(remainAmount + 1);
			remainAmount -= randomAmount;
			if (randomAmount > 0) { // 0원을 받는 사람은 저장X
				updatedMembers.add(new SettlementMember(
					settlementMemberIds.get(i),
					randomAmount,
					settlement
				));
			}
		}

		//남은 돈이 있으면 마지막 사람에게 할당
		if (remainAmount > 0) {
			updatedMembers.add(new SettlementMember(
				settlementMemberIds.get(totalPeople - 1),
				remainAmount,
				settlement
			));
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	private void queuePendingTransaction(long senderId, long recieverId, long amount) {
		try {
			String key = "transfer:" + senderId + ":" + recieverId + ":" + amount;
			redisTemplate.opsForValue().set(key, amount);
		} catch (Exception e) {
			log.error("송금 환불 실패 (트랜잭션 ID: {}): {}", senderId, e.getMessage());
			throw e;
		}
	}


	private void refundToSender(Transaction transaction) {
		try {
			String key = "transfer:" + transaction.getReceiverAccountId() + ":" + transaction.getSenderAccountId() + ":" + transaction.getAmount();
			redisTemplate.opsForValue().set(key, transaction.getAmount());
		} catch (Exception e) {
			log.error("송금 환불 실패 (트랜잭션 ID: {}): {}", transaction.getId(), e.getMessage());
			throw e;
		}
	}
}


