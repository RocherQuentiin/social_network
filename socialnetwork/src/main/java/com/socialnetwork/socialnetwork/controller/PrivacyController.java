package com.socialnetwork.socialnetwork.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IPrivacySettingsService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PrivacyController {
	private IPrivacySettingsService privacySettingsService;
	private IUserService userService; 
	
	public PrivacyController(IPrivacySettingsService privacySettingsService, IUserService userService) {
		this.privacySettingsService = privacySettingsService;
		this.userService = userService;
	}
	
	@GetMapping("/privacy")
    public String showPrivacyPage(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, true);
		model.addAttribute("isConnect", userIsConnect);
		if(userIsConnect == null) {
			return "accueil";
		}
		
		ResponseEntity<PrivacySettings> privacySettings = this.privacySettingsService.getPrivacySettingsByUserID(UUID.fromString(userIsConnect.toString()));
    
		model.addAttribute("privacy", privacySettings.getBody());
		
		return "privacySettings";
	}
	
	@PostMapping("/privacy")
    public String SavePrivacy(HttpServletRequest request, Model model, PrivacySettings privacySettings) {
        Object userIsConnect = Utils.validPage(request, true);
        model.addAttribute("isConnect", userIsConnect);
        if (userIsConnect == null) {
            return "accueil";
        }

        UUID userId = UUID.fromString(userIsConnect.toString());

        // 1. On récupère les paramètres actuels s'ils existent
        ResponseEntity<PrivacySettings> userPrivacy = this.privacySettingsService.getPrivacySettingsByUserID(userId);

        if (userPrivacy != null && userPrivacy.getBody() != null) {
            // CAS 1 : L'utilisateur a déjà des paramètres en BDD, on récupère son ID existant et son User associé
            privacySettings.setId(userPrivacy.getBody().getId());
            privacySettings.setUser(userPrivacy.getBody().getUser());
        } else {
            // CAS 2 : Première configuration ! L'objet n'existe pas encore en BDD.
            // Il faut obligatoirement lier l'utilisateur à ces paramètres pour que Hibernate puisse faire le lien.

            // IMPORTANT : Crée un objet User avec l'ID connecté et associe-le
            User user = new User();
            user.setId(userId);
            privacySettings.setUser(user);
        }

        // 2. On sauvegarde (Hibernate fera un UPDATE pour le CAS 1, ou un INSERT pour le CAS 2)
        ResponseEntity<PrivacySettings> privacy = this.privacySettingsService.savePrivacy(privacySettings);

        if (privacy != null && privacy.getBody() != null) {
            model.addAttribute("privacy", privacy.getBody());
            model.addAttribute("information", "Vos données privées ont bien été sauvegardées.");
        } else {
            model.addAttribute("privacy", privacySettings);
            model.addAttribute("error", "Erreur lors de la sauvegarde.");
        }

        return "privacySettings";
    }
}
