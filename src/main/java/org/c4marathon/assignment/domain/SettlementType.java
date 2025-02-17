package org.c4marathon.assignment.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.c4marathon.assignment.global.CommonUtils;

public enum SettlementType {
	/**
	 * 일반 정산은 금액을 전부 나누어 지급한 뒤, 남은 금액에 대해서는 랜덤하게 시작 사용자를 정하고,
	 * 그 시작 사용자부터 순차적으로 1원씩 추가 부과합니다.
	 */
	NORMAL((size, money) -> {
		List<Long> divided = new ArrayList<>();
		IntStream.range(0, size).forEach(i -> divided.add(money / size));

		if (money % size != 0) {
			int startIdx = CommonUtils.getRandom(0, size);

			IntStream.range(0, (int)(money % size)).forEach(plus -> {
				int nextIdx = (startIdx + plus) % size;
				divided.set(nextIdx, divided.get(nextIdx) + 1);
			});
		}

		return divided;
	}),
	/**
	 * 랜덤의 경우, 금액 내에서 경계를 (사용자 수 - 1) 만큼 랜덤 선택합니다. 그리고 경계를 기준으로 금액을 분배합니다.
	 */
	RANDOM((size, money) -> {
		List<Long> divided = new ArrayList<>();

		long[] boundaries = IntStream.range(0, size).mapToLong(i -> {
			if (i == size - 1)
				return money;

			return CommonUtils.getRandom(0, money + 1);
		}).sorted().toArray();

		divided.add(boundaries[0]);
		IntStream.range(1, size).forEach(idx -> divided.add(boundaries[idx] - boundaries[idx - 1]));

		return divided;
	});

	private final BiFunction<Integer, Long, List<Long>> settlementFunc;

	SettlementType(BiFunction<Integer, Long, List<Long>> settlementFunc) {
		this.settlementFunc = settlementFunc;
	}

	public List<Long> settle(int size, long money) {
		return settlementFunc.apply(size, money);
	}
}
