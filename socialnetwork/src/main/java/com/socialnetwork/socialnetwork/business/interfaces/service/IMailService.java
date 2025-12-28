package com.socialnetwork.socialnetwork.business.interfaces.service;

public interface IMailService {

	public void sendConfirmationAccountMail(String emailToSend, String code, String firstName);
	
	public void sendForgotPassword(String emailToSend, String code, String firstName);
}
