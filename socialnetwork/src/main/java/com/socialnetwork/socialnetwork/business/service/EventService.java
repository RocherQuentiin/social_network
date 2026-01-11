package com.socialnetwork.socialnetwork.business.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

@Override
public ResponseEntity<Event> getFirstEventByDate(UUID id){
	ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
	Optional<List<Event>> event = this.repository.getEventByDate(now.toLocalDateTime());
	
	if(event.get().size() == 0) {
		return new ResponseEntity<>(
			      HttpStatus.NOT_FOUND);
	}
	
	Optional<Event> eventElm =  event.get().stream().filter(x -> x.getCreator().getId().equals(id)).findFirst();
	
	if(!eventElm.isPresent()) {
		return new ResponseEntity<>(
			      HttpStatus.NOT_FOUND);
	}
	
	return new ResponseEntity<>(
			  eventElm.get(), 
		      HttpStatus.OK);
	
}

@Override
public ResponseEntity<Event> getEventByID(UUID id) {
	Optional<Event> event = this.repository.findById(id);
	
	if(!event.isPresent()) {
		return new ResponseEntity<>(
			      HttpStatus.NOT_FOUND);
	}
	
	return new ResponseEntity<>(
			  event.get(), 
		      HttpStatus.OK);
}

@Override
public ResponseEntity<Event> update(Event event) {
	Event saveEvent = this.repository.save(event);
	
	return new ResponseEntity<>(
			saveEvent, 
		      HttpStatus.OK);
}
}
