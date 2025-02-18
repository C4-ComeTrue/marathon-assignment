package org.c4marathon.assignment.common.util;

import java.util.Random;

public class BankSystemUtil {
	private static final String BANK_CODE = "3333";

	/**
	 * 계좌 번호 생성
	 * */
	public static String createAccountNumber(int counter){
		Random random = new Random();
		int createNum = 0;
		String ranNum = "";
		String randomNum = "";

		for (int i=0; i<7; i++) {
			createNum = random.nextInt(9);
			ranNum = Integer.toString(createNum);
			randomNum += ranNum;
		}
		String bankNum = "3333";
		String countAccountNum = String.format("%02d",counter);

		counter++;
		String accountNum = bankNum+"-"+countAccountNum+"-"+randomNum;
		return accountNum;
	}
}
