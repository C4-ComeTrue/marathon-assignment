package org.c4marathon.assignment.mail;

import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final EmailNotificationService emailNotificationService;
	private final MemberRepository memberRepository;

	public void sendRemindNotification(Transaction transaction) {
		Long receiverAccountId = transaction.getReceiverAccountId();
		Member receiverMember = memberRepository.findByAccountId(receiverAccountId)
			.orElseThrow(NotFoundMemberException::new);

		String subject = "[알림] 송금 마감";
		String content = "24시간 후 송금이 취소됩니다.";

		emailNotificationService.sendRemindEmail(receiverMember.getEmail(), subject, content);
	}
}
