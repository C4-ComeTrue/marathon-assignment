package org.c4marathon.assignment.global;

import java.security.SecureRandom;

public abstract class CommonUtils {
	private static final SecureRandom RANDOM = new SecureRandom();

	public static long getCeil(long val, long ceilingPoint) {
		long mul = (val + ceilingPoint - 1) / ceilingPoint;

		return ceilingPoint * mul;
	}

	public static long getRandom(long start, long end) {
		return RANDOM.nextLong(start, end);
	}

	public static int getRandom(int start, int end) {
		return RANDOM.nextInt(start, end);
	}
}
