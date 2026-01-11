package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Event;

public interface IEventService {

	ResponseEntity<Event> save(Event event);

	ResponseEntity<Event> getFirstEventByDate(UUID id);

	ResponseEntity<Event> getEventByID(UUID id);

	ResponseEntity<Event> update(Event body);

}
