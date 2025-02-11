package org.c4marathon.assignment.service;

import java.util.Set;

import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

	/**
	 * 적절한 스케줄러 시간을 어떻게 해야할지 고민
	 * (1) 너무 짧은 주기로 실행할 경우 CPU 부하가 높아지도 레디스에도 부하가 많아진다.
	 * 		=> 더 빨리 실시간 이체를 해야할 경우에는 메시지큐를 사용
	 * **/
	@Scheduled(fixedRate = 3000)
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void completeTransfer() {
		Set<String> keys = redisTemplate.keys("transfer:*");
		if (keys == null || keys.isEmpty())
			return;

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

		if (failCount == null) {failCount = 1L;}
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

				// mainAccountService.rollbackDailyChargeLimit(senderAccountId, chargeAmount); => 이미 충전한것을 되돌릴 필요는 없다 생각
				log.info("송금 롤백 완료: 보낸 사람 ID={}, 금액={}원 복구 완료", senderAccountId, chargeAmount);

				redisTemplate.delete(key);
				redisTemplate.delete(failKey);

			} catch (Exception rollbackException) {
				log.error("송금 롤백 실패: {}, 송금 transaction Info: {}", rollbackException.getMessage(),key);
				// 해당 레벨의 에러는 사람이 처리
			}


		}
	}
}