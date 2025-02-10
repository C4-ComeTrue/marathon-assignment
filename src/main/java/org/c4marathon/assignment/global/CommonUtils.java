package org.c4marathon.assignment.global;

public class CommonUtils {
	private CommonUtils() {}

	public static long getCeil(long val, long ceilingPoint) {
		long mul = (val + ceilingPoint - 1) / ceilingPoint;

		return ceilingPoint * mul;
	}
}
