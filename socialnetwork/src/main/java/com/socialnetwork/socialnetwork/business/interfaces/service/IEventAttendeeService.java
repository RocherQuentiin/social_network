package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.EventAttendee;

public interface IEventAttendeeService {

	ResponseEntity<EventAttendee> getEventAttendeeByEventIDAndUserID(UUID eventID, UUID fromString);

	ResponseEntity<EventAttendee>  saveEventAttendee(EventAttendee eventAttendee);

	ResponseEntity<List<EventAttendee>> getPendingFor(UUID userID);

	ResponseEntity<List<EventAttendee>> getSentRequestsFor(UUID requesterId);

	void deleteEventAttendeeByEventIdAndUserId(EventAttendee eventAttedee);

	public ResponseEntity<EventAttendee> Update(EventAttendee eventAttendee);

	ResponseEntity<List<EventAttendee>> getEventAttendeeByEventID(UUID eventID);

}
