package org.c4marathon.assignment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferScheduler {
	private final RedisTemplate<String, Long> redisTemplate;
	private final TransferService transferService;

	/**
	 * 적절한 스케줄러 시간을 어떻게 해야할지 고민
	 * (1) 너무 짧은 주기로 실행할 경우 CPU 부하가 높아지도 레디스에도 부하가 많아진다.
	 * 		=> 더 빨리 실시간 이체를 해야할 경우에는 메시지큐를 사용
	 * **/
	@Scheduled(fixedRate = 3000) //3초
	public void completeTransfer() {
		int batchSize = 1000;
		String cursor = "0";
		do {
			Cursor<byte[]> scanCursor = redisTemplate.getConnectionFactory().getConnection()
				.scan(ScanOptions.scanOptions().match("transfer:*").count(batchSize).build());

			List<String> keys = new ArrayList<>(); //1000개씩 스캔한 키들

			while (scanCursor.hasNext()) {
				keys.add(new String(scanCursor.next()));

				if (keys.size() >= batchSize) {
					transferService.transferBatch(keys);
					keys.clear();
				}
			}

			if (!keys.isEmpty()) { //맨 마지막 남은 나머지 키들
				transferService.transferBatch(keys);
			}

			cursor = String.valueOf(scanCursor.getCursorId());
		} while (!cursor.equals("0"));
	}

}
