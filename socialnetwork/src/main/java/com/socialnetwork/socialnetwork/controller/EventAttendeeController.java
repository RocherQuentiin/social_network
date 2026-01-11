package com.socialnetwork.socialnetwork.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventAttendeeService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.EventAttendeeDto;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.EventAttendee;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.EventAttendanceStatus;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("eventattendee")
public class EventAttendeeController {
	private IEventAttendeeService eventAttendeeservice;
	private IEventService eventservice;
	private IUserService userService;

	public EventAttendeeController(IEventAttendeeService eventAttendeeservice, IUserService userService,
			IEventService eventservice) {
		this.eventAttendeeservice = eventAttendeeservice;
		this.eventservice = eventservice;
		this.userService = userService;
	}

	@PostMapping("")
	public ResponseEntity<String> createEventAttendee(HttpServletRequest request,
			@RequestBody EventAttendeeDto eventAttendeeDto) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		ResponseEntity<EventAttendee> eventAttendeeExist = this.eventAttendeeservice.getEventAttendeeByEventIDAndUserID(
				UUID.fromString(eventAttendeeDto.getEventID()), UUID.fromString(userIsConnect.toString()));

		if (eventAttendeeExist.getStatusCode() == HttpStatusCode.valueOf(200)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already try to join the event");
		}

		ResponseEntity<Event> event = this.eventservice.getEventByID(UUID.fromString(eventAttendeeDto.getEventID()));

		if (event.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("event not exist");
		}

		if (event.getBody().getCreator().getId().equals(UUID.fromString(userIsConnect.toString()))) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Creator can't join his own event");
		}

		ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));

		EventAttendee eventAttendee = new EventAttendee();

		eventAttendee.setEvent(event.getBody());
		eventAttendee.setUser(user.getBody());

		this.eventAttendeeservice.saveEventAttendee(eventAttendee);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/pending")
	public ResponseEntity<List<EventAttendee>> getPendingRequests(HttpServletRequest request) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		UUID receiverId = UUID.fromString(userIsConnect.toString());
		return this.eventAttendeeservice.getPendingFor(receiverId);
	}

	@GetMapping("/sent")
	public ResponseEntity<List<EventAttendee>> getSentRequests(HttpServletRequest request) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		UUID requesterId = UUID.fromString(userIsConnect.toString());
		return this.eventAttendeeservice.getSentRequestsFor(requesterId);
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteEventAttendee(HttpServletRequest request, @PathVariable("id") UUID id) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		ResponseEntity<EventAttendee> eventAttendeeExist = this.eventAttendeeservice.getEventAttendeeByEventIDAndUserID(
				id, UUID.fromString(userIsConnect.toString()));

		if (eventAttendeeExist.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not exist in this event");
		}
		
		this.eventAttendeeservice.deleteEventAttendeeByEventIdAndUserId(eventAttendeeExist.getBody());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/accept")
	public ResponseEntity<String> acceptEventRequest(HttpServletRequest request,
			@RequestParam("requesterId") String requesterId ,
			@RequestParam("eventId") String eventId) {
		System.out.println("ok");
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		UUID reqId = UUID.fromString(requesterId);
		UUID eventUUId = UUID.fromString(eventId);
		
		ResponseEntity<EventAttendee> eventAttendeeExist = this.eventAttendeeservice.getEventAttendeeByEventIDAndUserID(
				eventUUId, reqId);
		System.out.println("ok");
		if (eventAttendeeExist.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not exist in this event");
		}
		
		eventAttendeeExist.getBody().setStatus(EventAttendanceStatus.ACCEPTED);
		
		ResponseEntity<EventAttendee> resp = this.eventAttendeeservice.Update(eventAttendeeExist.getBody());
		if (resp.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity.status(HttpStatus.OK).body("Event request accepted");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to accept event request");
	}

	@PutMapping("/decline")
	public ResponseEntity<String> declineFriendRequest(HttpServletRequest request,
			@RequestParam("requesterId") String requesterId,
			@RequestParam("eventId") String eventId) {
		System.out.println("ok");
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		UUID reqId = UUID.fromString(requesterId);
		UUID eventUUId = UUID.fromString(eventId);
		
		ResponseEntity<EventAttendee> eventAttendeeExist = this.eventAttendeeservice.getEventAttendeeByEventIDAndUserID(
				eventUUId, reqId);

		if (eventAttendeeExist.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not exist in this event");
		}
		
		eventAttendeeExist.getBody().setStatus(EventAttendanceStatus.DECLINED);
		
		ResponseEntity<EventAttendee> resp = this.eventAttendeeservice.Update(eventAttendeeExist.getBody());
		if (resp.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity.status(HttpStatus.OK).body("Event request accepted");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to accept event request");
	}
	
}


