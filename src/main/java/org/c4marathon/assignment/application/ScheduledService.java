package org.c4marathon.assignment.application;

import java.util.List;

import org.c4marathon.assignment.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledService {
	private final UserService userService;

	/**
	 * 유저 인당 한도 초기화 벌크 연산
	 */
	@Scheduled(cron = "${user.charge-limit-initialization.execution-time}")
	public void initChargeLimit() {
		int limit = 1000;
		List<User> users = userService.findAllByCursor(0, limit);
		while (!users.isEmpty()) {
			List<Long> userIds = users.stream().mapToLong(User::getId).boxed().toList();

			userService.initChargeLimit(userIds);

			users = userService.findAllByCursor(users.get(users.size() - 1).getId(), limit);
		}

	}
}
