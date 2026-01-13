package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventAttendeeRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventAttendeeService;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.EventAttendee;
import com.socialnetwork.socialnetwork.enums.EventAttendanceStatus;

@Service
public class EventAttendeeService implements IEventAttendeeService{

	private IEventAttendeeRepository repository;
	
	public EventAttendeeService(IEventAttendeeRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public ResponseEntity<EventAttendee> getEventAttendeeByEventIDAndUserID(UUID eventID, UUID userID) {
		Optional<EventAttendee> eventAttendee = this.repository.findByidAndUser_id(eventID,userID);
		
		if(!eventAttendee.isPresent()) {
			return new ResponseEntity<>(
				      HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(
				eventAttendee.get(), 
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<EventAttendee> saveEventAttendee(EventAttendee eventAttendee) {
		EventAttendee saveEventAttendee = this.repository.save(eventAttendee);
		return new ResponseEntity<>(
				saveEventAttendee, 
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<EventAttendee>> getPendingFor(UUID userID) {
		List<EventAttendee> listEventAttendee = this.repository.findBystatusAndEvent_creator_id(EventAttendanceStatus.PENDING, userID);
		return new ResponseEntity<List<EventAttendee>>(listEventAttendee, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<EventAttendee>> getSentRequestsFor(UUID requesterId) {
		List<EventAttendee> listEventAttendee = this.repository.findBystatusAndUser_id(EventAttendanceStatus.PENDING, requesterId);
		System.out.println(listEventAttendee.get(0).getUser().getFirstName());
		return new ResponseEntity<List<EventAttendee>>(listEventAttendee, HttpStatus.OK);
	}
	

}
