package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.socialnetwork.socialnetwork.business.interfaces.service.IMailService;

public class MailService implements IMailService {

	private final String from = "isepsocial@outlook.fr";
	private String host = "smtp.office365.com";
	private final String username = "isepsocial@outlook.fr";
	private final String password = "uypjdxtshfucwhey";
	private Session session;

	public MailService() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");

		session = Session.getInstance(props, new jakarta.mail.Authenticator() {
			protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
	}

	public void sendConfirmationAccountMail(String emailToSend) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailToSend));
			message.setSubject("Test Email");
			message.setText("This is a test email sent from Java.");

			// Send the email
			Transport.send(message);
			System.out.println("Email sent successfully.");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}
