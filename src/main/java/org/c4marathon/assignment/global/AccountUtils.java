package org.c4marathon.assignment.global;

import java.util.stream.IntStream;

public abstract class AccountUtils {
	static final int LIMIT = 13;

	public static String getAccountNumber() {
		StringBuilder accountNumber = new StringBuilder();
		IntStream.range(0, LIMIT).forEach(i -> accountNumber.append((char)('0' + CommonUtils.getRandom(0, 10))));
		return accountNumber.toString();
	}
}
