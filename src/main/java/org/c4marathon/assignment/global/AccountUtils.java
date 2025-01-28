package org.c4marathon.assignment.global;

import java.util.Random;
import java.util.stream.IntStream;

public class AccountUtils {
	static final Random RANDOM = new Random();
	static final int LIMIT = 13;

	public static String getAccountNumber() {
		StringBuilder accountNumber = new StringBuilder();
		IntStream.range(0, LIMIT).forEach(i -> accountNumber.append((char)('0' + RANDOM.nextInt(10))));
		return accountNumber.toString();
	}
}
