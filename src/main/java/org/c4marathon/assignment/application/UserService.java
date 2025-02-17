package org.c4marathon.assignment.application;

import java.util.List;
import java.util.stream.IntStream;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.SettlementType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.request.SettlementRequest;
import org.c4marathon.assignment.domain.dto.response.SettlementResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;

	@Transactional
	public boolean register(String email) {
		User createdUser = User.builder().email(email).build();
		userRepository.save(createdUser);

		Account account = Account.builder().isMain(true).userId(createdUser.getId()).build();
		accountRepository.save(account);

		return true;
	}

	public List<User> findAllByCursor(long cursor, int limit) {
		return userRepository.findAllByCursor(cursor, limit);
	}

	@Transactional
	public void initChargeLimit(List<Long> userIds) {
		userRepository.initChargeLimit(userIds);
	}

	/**
	 * 정산 기능. 선택된 사용자만큼 타입에 따라 정산합니다.
	 * @param request
	 * @return
	 */
	public List<SettlementResult> settle(SettlementRequest request) {
		List<Long> userIds = request.userIds();
		long money = request.money();
		SettlementType settlementType = request.settlementType();

		List<User> users = userRepository.findAllById(userIds);
		List<Long> settled = settlementType.settle(users.size(), money);

		return IntStream.range(0, users.size())
			.mapToObj(idx ->
				new SettlementResult(users.get(idx).getId(), users.get(idx).getEmail(), settled.get(idx)))
			.toList();
	}
}
