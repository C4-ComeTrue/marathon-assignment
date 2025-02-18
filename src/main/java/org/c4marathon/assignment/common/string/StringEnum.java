package org.c4marathon.assignment.common.string;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StringEnum {
	PENDING_MAIL_TITLE("송금 요청이 도착했습니다. 72시간 내에 수령해주세요!"),
	ALERT_24_MAIL_TITLE("24시간 내에 송금을 받지 않을 경우 송금 취소됩니다. 24시간 내에 수령해주세요!"),
	REMITTANCE_FAIL_MAIL_TITLE( "72시간이 지나 송금이 자동 취소되었습니다.");

	private final String message;

	@Override
	public String toString() {
		return message;
	}
}
