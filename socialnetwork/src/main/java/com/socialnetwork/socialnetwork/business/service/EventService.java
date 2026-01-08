package com.socialnetwork.socialnetwork.business.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.entity.Event;

@Service
public class EventService implements IEventService{
private IEventRepository repository;

public EventService(IEventRepository repository) {
	this.repository = repository;
}

@Override
public ResponseEntity<Event> save(Event event) {
	Event saveEvent = this.repository.save(event);
	
	return new ResponseEntity<>(
			  saveEvent, 
		      HttpStatus.OK);
}


}
