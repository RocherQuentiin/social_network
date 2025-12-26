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

import io.github.cdimascio.dotenv.Dotenv;

public class MailService implements IMailService {

	private final String from = "isepsocial@outlook.fr";
	private String host = "sandbox.smtp.mailtrap.io";
	private final String username = "2db2becb64d611";
	private final String password = "dbdfb555556d71";
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

	public void sendConfirmationAccountMail(String emailToSend, String code, String firstName) {
		try {
			Dotenv dotenv = Dotenv.load();
			System.out.println(dotenv.get("FRONT_BASE_URL"));
			String confirmationLink = dotenv.get("FRONT_BASE_URL") + "/user/" + code + "/confirm";
			System.out.println(confirmationLink);
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailToSend));
			message.setSubject("Confirmation Création de compte");
			
			String htmlContent = "<!DOCTYPE html>\r\n"
					+ "<html lang=\"fr\" style=\"margin:0; padding:0;\">\r\n"
					+ "<head>\r\n"
					+ "  <meta charset=\"UTF-8\">\r\n"
					+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
					+ "  <title>Confirmation d'inscription</title>\r\n"
					+ "  <style>\r\n"
					+ "    body {\r\n"
					+ "      font-family: Arial, sans-serif;\r\n"
					+ "      background-color: #f6f6f6;\r\n"
					+ "      margin: 0;\r\n"
					+ "      padding: 0;\r\n"
					+ "    }\r\n"
					+ "    .container {\r\n"
					+ "      max-width: 600px;\r\n"
					+ "      margin: 0 auto;\r\n"
					+ "      background-color: #ffffff;\r\n"
					+ "      border-radius: 8px;\r\n"
					+ "      overflow: hidden;\r\n"
					+ "      box-shadow: 0 2px 6px rgba(0,0,0,0.1);\r\n"
					+ "    }\r\n"
					+ "    .header {\r\n"
					+ "      background-color: #2d89ef;\r\n"
					+ "      color: #ffffff;\r\n"
					+ "      text-align: center;\r\n"
					+ "      padding: 20px;\r\n"
					+ "    }\r\n"
					+ "    .header h1 {\r\n"
					+ "      margin: 0;\r\n"
					+ "      font-size: 24px;\r\n"
					+ "    }\r\n"
					+ "    .content {\r\n"
					+ "      padding: 30px;\r\n"
					+ "      color: #333333;\r\n"
					+ "      line-height: 1.6;\r\n"
					+ "    }\r\n"
					+ "    .btn {\r\n"
					+ "      display: inline-block;\r\n"
					+ "      background-color: #2d89ef;\r\n"
					+ "      color: #ffffff !important;\r\n"
					+ "      padding: 12px 24px;\r\n"
					+ "      border-radius: 4px;\r\n"
					+ "      text-decoration: none;\r\n"
					+ "      font-weight: bold;\r\n"
					+ "      margin-top: 20px;\r\n"
					+ "    }\r\n"
					+ "    .footer {\r\n"
					+ "      font-size: 12px;\r\n"
					+ "      color: #888888;\r\n"
					+ "      text-align: center;\r\n"
					+ "      padding: 20px;\r\n"
					+ "    }\r\n"
					+ "    @media (max-width: 600px) {\r\n"
					+ "      .content {\r\n"
					+ "        padding: 20px;\r\n"
					+ "      }\r\n"
					+ "    }\r\n"
					+ "  </style>\r\n"
					+ "</head>\r\n"
					+ "<body>\r\n"
					+ "  <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color:#f6f6f6;\">\r\n"
					+ "    <tr>\r\n"
					+ "      <td align=\"center\">\r\n"
					+ "        <div class=\"container\">\r\n"
					+ "          <!-- Header -->\r\n"
					+ "          <div class=\"header\">\r\n"
					+ "            <h1>Bienvenue !</h1>\r\n"
					+ "          </div>\r\n"
					+ "          <!-- Content -->\r\n"
					+ "          <div class=\"content\">\r\n"
					+ "            <p>Bonjour <strong>[Prénom]</strong>,</p>\r\n"
					+ "            <p>Merci de vous être inscrit(e) sur <strong>ISEP Social network</strong> !</p>\r\n"
					+ "            <p>Pour finaliser votre inscription et activer votre compte, veuillez cliquer sur le bouton ci-dessous :</p>\r\n"
					+ "            <p style=\"text-align:center;\">\r\n"
					+ "              <a href=\"[LIEN_DE_CONFIRMATION]\" class=\"btn\">Confirmer mon inscription</a>\r\n"
					+ "            </p>\r\n"
					+ "            <p>Si le bouton ne fonctionne pas, copiez et collez ce lien dans votre navigateur :</p>\r\n"
					+ "            <p style=\"word-break: break-all; color:#2d89ef;\">[LIEN_DE_CONFIRMATION]</p>\r\n"
					+ "            <p>Merci et à très bientôt,<br>L’équipe <strong>ISEP Social Network</strong></p>\r\n"
					+ "          </div>\r\n"
					+ "          <!-- Footer -->\r\n"
					+ "          <div class=\"footer\">\r\n"
					+ "            <p>Vous recevez cet e-mail car vous avez créé un compte sur ISEP Social Network.  \r\n"
					+ "            Si vous n’êtes pas à l’origine de cette demande, vous pouvez ignorer ce message.</p>\r\n"
					+ "          </div>\r\n"
					+ "        </div>\r\n"
					+ "      </td>\r\n"
					+ "    </tr>\r\n"
					+ "  </table>\r\n"
					+ "</body>\r\n"
					+ "</html>\r\n";
					
			
			htmlContent = htmlContent.replace("[LIEN_DE_CONFIRMATION]", confirmationLink).replace("[Prénom]", firstName);
			
			message.setContent(htmlContent, "text/html; charset=UTF-8");

			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}
