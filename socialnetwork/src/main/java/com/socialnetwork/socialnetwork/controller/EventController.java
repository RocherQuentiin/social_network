package com.socialnetwork.socialnetwork.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.EventDto;
import com.socialnetwork.socialnetwork.dto.PostDto;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

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
public String createEvent(HttpServletRequest request, Model model, Event event) {
    Object userIsConnect = Utils.validPage(request, true);
    model.addAttribute("isConnect", userIsConnect);
    if (userIsConnect == null) {
        return "accueil";
    }
    
    if(event.getName().trim().equals("") || event.getEventDate() == null || event.getLocation().trim().equals("") || event.getVisibilityType().equals("")) {
    	model.addAttribute("errorEvent", "Veuillez remplir l'ensemble des champs");
		model.addAttribute("event", event);
		return this.UserController.showUserProfil(request, model);
    }
    
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
    
    if(event.getEventDate().isBefore(now.toLocalDateTime()) || event.getEventDate().isEqual(now.toLocalDateTime())) {
    	model.addAttribute("errorEvent", "La date doit être supérieur a celle d'aujourd'hui pour la création de l'événement");
		model.addAttribute("event", event);
		return this.UserController.showUserProfil(request, model);
    }
    
    ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
    
    event.setCreator(user.getBody());
    
    ResponseEntity<Event> saveEvent = this.eventService.save(event);
    
    model.addAttribute("informationEvent", "L'événement a bien été crée");
	model.addAttribute("event", saveEvent);
	
	
	return this.UserController.showUserProfil(request, model);
}


@GetMapping("{id}")
public String GetEvent(HttpServletRequest request, Model model, @PathVariable("id") String id) {
    Object userIsConnect = Utils.validPage(request, true);
    model.addAttribute("isConnect", userIsConnect);
    if (userIsConnect == null) {
        return "accueil";
    }

    ResponseEntity<Event> event = this.eventService.getEventByID(UUID.fromString(id));

    if(event.getStatusCode() != HttpStatusCode.valueOf(200)) {
    	return "accueil";
    }

	model.addAttribute("eventinfo", event.getBody());

	return "event";
}

@PutMapping("{id}")
public ResponseEntity<?> updatePost(@PathVariable("id") UUID id, @RequestBody EventDto body, HttpServletRequest request) {
	Object userIsConnect = Utils.validPage(request, true);
    if (userIsConnect == null) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
    }
    
    ResponseEntity<Event> event = this.eventService.getEventByID(id);
    
    if (event.getStatusCode() == HttpStatusCode.valueOf(404)) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
    }
    
    if(!userIsConnect.toString().equals(event.getBody().getCreator().getId().toString())) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can edit");
    }
    
    if(body.getEventName().trim().equals("") || body.getEventDate() == null || body.getEventLocation().trim().equals("") || body.getEventVisibility().equals("")) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Fill all the inputs");
    }
	
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
    LocalDateTime eventDate = LocalDateTime.parse(body.getEventDate());
    
    if(eventDate.isBefore(now.toLocalDateTime()) || eventDate.isEqual(now.toLocalDateTime())) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The date must be superior than the actual date");
    }
    
    event.getBody().setName(body.getEventName());
    event.getBody().setVisibilityType(body.getEventVisibility());
    event.getBody().setEventDate(eventDate);
    event.getBody().setLocation(body.getEventLocation());
    event.getBody().setDescription(body.getEventDescription());

    
    event = this.eventService.update(event.getBody());
    
    return ResponseEntity.ok().build();
}
}
