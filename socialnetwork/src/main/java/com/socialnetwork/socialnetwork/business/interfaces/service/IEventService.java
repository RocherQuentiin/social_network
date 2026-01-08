package com.socialnetwork.socialnetwork.business.interfaces.service;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Event;

public interface IEventService {

	ResponseEntity<Event> save(Event event);

}
