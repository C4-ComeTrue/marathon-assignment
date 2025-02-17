package org.c4marathon.assignment.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.username}")
	private String username;
	@Value("${spring.mail.password}")
	private String password;
	@Value("${spring.mail.port}")
	private int port;


	@Bean
	public JavaMailSender getJavaMailSender() {
		var mailSender = new JavaMailSenderImpl();

		mailSender.setUsername(username);
		mailSender.setHost(host);
		mailSender.setPassword(password);
		mailSender.setPort(port);

		return mailSender;
	}
}