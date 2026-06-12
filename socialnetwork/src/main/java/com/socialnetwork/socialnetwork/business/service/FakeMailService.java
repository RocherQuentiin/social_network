package com.socialnetwork.socialnetwork.business.service;

import java.util.logging.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.socialnetwork.socialnetwork.business.interfaces.service.IMailService;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class FakeMailService implements IMailService {

    private static final Logger LOGGER = Logger.getLogger(FakeMailService.class.getName());

    @Override
    public void sendConfirmationAccountMail(String emailToSend, String code, String firstName) {
        LOGGER.info("[FAKEMAIL] Inscription de " + firstName + " -> Lien simulé : /user/" + code + "/confirm");
    }

    @Override
    public void sendForgotPassword(String emailToSend, String code, String firstName) {
        LOGGER.info("[FAKEMAIL] Mot de passe oublié pour " + firstName + " -> Lien simulé : /user/" + code + "/forgotpassword");
    }
}