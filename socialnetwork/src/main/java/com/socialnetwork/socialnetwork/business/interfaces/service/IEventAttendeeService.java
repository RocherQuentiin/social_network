package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.EventAttendee;

public interface IEventAttendeeService {

	ResponseEntity<EventAttendee> getEventAttendeeByEventIDAndUserID(UUID eventID, UUID fromString);

	ResponseEntity<EventAttendee>  saveEventAttendee(EventAttendee eventAttendee);

}
