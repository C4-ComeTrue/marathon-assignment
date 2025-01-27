package org.c4marathon.assignment.common.cache;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheScheduler {
	private final RedisTemplate<String, Long> redisTemplate;

	public CacheScheduler(RedisTemplate<String, Long> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void resetDailyLimit() {
		/* 24시에 10분동안 은행 계좌 점검 => 10분동안 일일 한도 초기화 */
		String lockKey = "reset";
		redisTemplate.opsForValue().set(lockKey, 1L, Duration.ofMinutes(10));

		/* 모든 dailyLimit 삭제 */
		try {
			redisTemplate.keys("dailyLimit:*").forEach(redisTemplate::delete);
		} finally {
			redisTemplate.delete(lockKey);
		}
	}
}
