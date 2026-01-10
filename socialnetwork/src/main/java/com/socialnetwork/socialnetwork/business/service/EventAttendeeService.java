package com.socialnetwork.socialnetwork.business.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventAttendeeRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventAttendeeService;
import com.socialnetwork.socialnetwork.entity.EventAttendee;

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

}
