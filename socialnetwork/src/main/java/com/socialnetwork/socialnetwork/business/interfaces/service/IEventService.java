package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Event;


public interface IEventService {

	ResponseEntity<Event> save(Event event);

	ResponseEntity<Event> getFirstEventByDate(UUID id);

	ResponseEntity<Event> getEventByID(UUID id);

	ResponseEntity<Event> update(Event body);

	ResponseEntity<List<Event>> getAllEventVisibilityPublic();

	ResponseEntity<List<Event>> getAllEventForConnectedUser(UUID userID, LocalDateTime localDateTime);

}
