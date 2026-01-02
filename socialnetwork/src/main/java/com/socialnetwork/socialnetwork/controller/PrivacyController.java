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
		if(userIsConnect == null) {
			return "accueil";
		}
		System.out.println(privacySettings.getCanSeeLocation());
		ResponseEntity<PrivacySettings> userPrivacy = this.privacySettingsService.getPrivacySettingsByUserID(UUID.fromString(userIsConnect.toString()));
    
		privacySettings.setId(userPrivacy.getBody().getId());
		privacySettings.setUser(userPrivacy.getBody().getUser());
		
		ResponseEntity<PrivacySettings> privacy = this.privacySettingsService.savePrivacy(privacySettings);
		
		model.addAttribute("privacy", privacy.getBody());
		model.addAttribute("information", "Vos données privée ont bien été sauvegarder");
		
		
		return "privacySettings";
	}
}
