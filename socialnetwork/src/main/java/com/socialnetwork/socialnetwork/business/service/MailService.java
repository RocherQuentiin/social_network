package com.socialnetwork.socialnetwork.business.service;

import java.net.PasswordAuthentication;
import java.util.List;

import com.socialnetwork.socialnetwork.business.interfaces.service.IMailService;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;



public class MailService implements IMailService {

	private final String Token = "7627ef82afcc38c1ff1e66ad839c076a";
	
	private MailtrapConfig  config;

	public MailService() {
		this.config = new MailtrapConfig.Builder()
	            .token(Token)
	            .build();
	}

	public void sendConfirmationAccountMail(String emailToSend) {
		MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);
		
		MailtrapMail mail = MailtrapMail.builder()
	            .from(new Address("socialisep@isep.fr", "SocialIsep"))
	            .to(List.of(new Address(emailToSend)))
	            .subject("Test send mail")
	            .text("Congrats for sending test email with Mailtrap!")
	            .category("Integration Test")
	            .build();

	        try {
	            System.out.println(client.send(mail));
	        } catch (Exception e) {
	            System.out.println("Caught exception : " + e);
	        }
	}

}
