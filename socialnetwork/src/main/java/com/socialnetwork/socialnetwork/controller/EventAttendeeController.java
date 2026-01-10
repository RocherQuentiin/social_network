package com.socialnetwork.socialnetwork.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventAttendeeService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.EventAttendeeDto;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.EventAttendee;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("eventattendee")
public class EventAttendeeController {
private IEventAttendeeService eventAttendeeservice;
private IEventService eventservice;
private IUserService userService;
public EventAttendeeController(IEventAttendeeService eventAttendeeservice, IUserService userService, IEventService eventservice) {
	this.eventAttendeeservice = eventAttendeeservice;
	this.eventservice = eventservice;
	this.userService = userService;
}
	
@PostMapping("")
public ResponseEntity<String> createEventAttendee(HttpServletRequest request, @RequestBody EventAttendeeDto eventAttendeeDto) {
	Object userIsConnect = Utils.validPage(request, true);
    if (userIsConnect == null) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
    }

    ResponseEntity<EventAttendee> eventAttendeeExist = this.eventAttendeeservice.getEventAttendeeByEventIDAndUserID(UUID.fromString(eventAttendeeDto.getEventID()), UUID.fromString(userIsConnect.toString()));

    if(eventAttendeeExist.getStatusCode() == HttpStatusCode.valueOf(200)) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already try to join the event");
    }
    
    ResponseEntity<Event> event = this.eventservice.getEventByID(UUID.fromString(eventAttendeeDto.getEventID()));
    
    if(event.getStatusCode() == HttpStatusCode.valueOf(404)) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("event not exist");
    }
    
    if(event.getBody().getCreator().getId().equals(UUID.fromString(userIsConnect.toString()))) {
    	return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Creator can't join his own event");
    }
    
    ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
    
    EventAttendee eventAttendee = new EventAttendee();
    
    eventAttendee.setEvent(event.getBody());
    eventAttendee.setUser(user.getBody());
    
    this.eventAttendeeservice.saveEventAttendee(eventAttendee);
    
    return ResponseEntity.ok().build();
}

}
