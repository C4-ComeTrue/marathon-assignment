package org.c4marathon.assignment.service;

import java.util.List;

import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
	private final MainAccountService mainAccountService;
	private final MainAccountRepository mainAccountRepository;
	private final RedisTemplate<String, Long> redisTemplate;
	private static final int RETRY_COUNT = 3;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void transferBatch(List<String> keys) {
		for (String key : keys) {
			try {
				String[] parts = key.split(":");
				long receiverAccountId = Long.parseLong(parts[2]);
				long amount = redisTemplate.opsForValue().get(key);

				MainAccount receiverAccount = mainAccountService.getMainAccountWithXLock(receiverAccountId);
				receiverAccount.updateBalance(amount);
				mainAccountRepository.save(receiverAccount);

				redisTemplate.delete(key);
			} catch (Exception e) {
				handleFailedTransfer(key, e);
			}
		}
	}

	private void handleFailedTransfer(String key, Exception e) {
		String failKey = "failed_transfer:" + key;
		Long failCount = redisTemplate.opsForValue().increment(failKey); // 실패 횟수 증가

		if (failCount == null) {
			failCount = 1L;
		}
		log.error("송금 처리 실패 ({}회), key: {}, 무슨 에러가 난건지: {}", failCount, key, e.getMessage());

		if (failCount >= RETRY_COUNT) {
			log.error("송금 3회 이상 실패로 인한 송금 롤백");
			try {
				String[] parts = key.split(":");
				long senderAccountId = Long.parseLong(parts[1]);
				long chargeAmount = Long.parseLong(parts[3]); //충전금액(한도 복구를 위해)
				long amount = redisTemplate.opsForValue().get(key);

				MainAccount sender = mainAccountService.getMainAccountWithXLock(senderAccountId);
				sender.updateBalance(amount);
				sender.updateLimit(chargeAmount); // 돈 다시 추가
				mainAccountRepository.save(sender);

				log.info("송금 롤백 완료: 보낸 사람 ID={}, 금액={}원 복구 완료", senderAccountId, chargeAmount);

				redisTemplate.delete(key);
				redisTemplate.delete(failKey);

			} catch (Exception rollbackException) {
				log.error("송금 롤백 실패: {}, 송금 transaction Info: {}", rollbackException.getMessage(), key);
				// 해당 레벨의 에러는 사람이 처리
			}

		}
	}
}