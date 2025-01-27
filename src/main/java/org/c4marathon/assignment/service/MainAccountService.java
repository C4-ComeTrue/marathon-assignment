package org.c4marathon.assignment.service;

import java.util.Random;
import org.c4marathon.assignment.common.exception.NotFoundException;
import org.c4marathon.assignment.common.exception.enums.ErrorCode;
import org.c4marathon.assignment.domain.MainAccount;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.MainAccountRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@RequiredArgsConstructor
public class MainAccountService {
	private final MainAccountRepository mainAccountRepository;
	static int counter = 1;

	@Transactional
	public void createMainAccount(User user){
		MainAccount mainAccount = new MainAccount(user, createAccountNumber(), 0,3000000);
		mainAccountRepository.save(mainAccount);
	}

	public MainAccount getMainAccount(long mainAccountId){
		return mainAccountRepository.findById(mainAccountId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MAIN_ACCOUNT));
	}

	/**
	 * 계좌 번호 생성
	 * */
	private String createAccountNumber(){
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
