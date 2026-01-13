package com.socialnetwork.socialnetwork.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialnetwork.socialnetwork.business.interfaces.service.IRecommandationService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Recommendation;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/recommandation")
public class RecommandationController {
	private IRecommandationService recommandationService;
	private IUserService userService;
	@Autowired
	private UserController UserController;

	public RecommandationController(IRecommandationService recommandationService, IUserService userService) {
		this.recommandationService = recommandationService;
		this.userService = userService;
	}
	
	@PostMapping("")
	public String recommandationUser(HttpServletRequest request, Model model, Recommendation recommandation, @RequestParam("userRecommandationID") UUID recommandedUserID) {
		Object userIsConnect = Utils.validPage(request, true);
		model.addAttribute("isConnect", userIsConnect);
		if (userIsConnect == null) {
			return "accueil";
		}
		
		ResponseEntity<Recommendation> recommandationExist = this.recommandationService.getRecommandationByUserIDAndRecommandedUserID(UUID.fromString(userIsConnect.toString()), recommandedUserID);
	
		if(recommandationExist.getStatusCode() == HttpStatusCode.valueOf(200)) {
			recommandationExist.getBody().setScore(recommandation.getScore());
			recommandationExist.getBody().setReason(recommandation.getReason());
			ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
			recommandationExist.getBody().setUpdatedAt(now.toLocalDateTime());
			
			this.recommandationService.saveRecommandation(recommandationExist.getBody());
			model.addAttribute("information", "Votre récommandation a bien était prise en compte");
			return this.UserController.showOtherUserProfil(request, model, recommandedUserID.toString());
		}
		else {
			Recommendation recommandationUser = new Recommendation();
			recommandationUser.setScore(recommandation.getScore());
			recommandationUser.setReason(recommandation.getReason());
			
			ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
			
			if(user.getStatusCode() == HttpStatusCode.valueOf(404)) {
				model.addAttribute("error", "Utilisateur introuvable");
				return this.UserController.showOtherUserProfil(request, model, recommandation.getUser().getId().toString());
			}
			
			ResponseEntity<User> recommandedUser = this.userService.getUserById(recommandedUserID);
			
			if(recommandedUser.getStatusCode() == HttpStatusCode.valueOf(404)) {
				model.addAttribute("error", "Utilisateur recommandé introuvable");
				return this.UserController.showOtherUserProfil(request, model, recommandedUserID.toString());
			}
			
			recommandationUser.setUser(user.getBody());
			recommandationUser.setRecommendedUser(recommandedUser.getBody());
			this.recommandationService.saveRecommandation(recommandationUser);
			model.addAttribute("information", "Votre récommandation a bien était prise en compte");
			return this.UserController.showOtherUserProfil(request, model, recommandedUserID.toString());
		}
	}
}
