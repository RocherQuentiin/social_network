package com.socialnetwork.socialnetwork.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/event")
public class EventController {
private IEventService eventService;
private IUserService userService;

@Autowired
private UserController UserController;

public EventController(IEventService eventService, IUserService userService) {
	this.eventService = eventService;
	this.userService = userService;
}

@PostMapping("")
public String sendFriendRequest(HttpServletRequest request, Model model, Event event) {
    Object userIsConnect = Utils.validPage(request, true);
    model.addAttribute("isConnect", userIsConnect);
    if (userIsConnect == null) {
        return "accueil";
    }
    
    if(event.getName().trim().equals("") || event.getEventDate() == null || event.getLocation().trim().equals("") || event.getVisibilityType().equals("")) {
    	model.addAttribute("error", "Veuillez remplir l'ensemble des champs");
		model.addAttribute("event", event);
		return this.UserController.showUserProfil(request, model);
    }
    
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
    
    if(event.getEventDate().isBefore(now.toLocalDateTime()) || event.getEventDate().isEqual(now.toLocalDateTime())) {
    	model.addAttribute("error", "La date doit être spérieur a celle d'aujourd'hui");
		model.addAttribute("event", event);
		return this.UserController.showUserProfil(request, model);
    }
    
    ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
    
    event.setCreator(user.getBody());
    
    ResponseEntity<Event> saveEvent = this.eventService.save(event);
    
    model.addAttribute("information", "L'événement a bien été crée");
	model.addAttribute("event", saveEvent);
	
	
	return this.UserController.showUserProfil(request, model);
   
}
}
