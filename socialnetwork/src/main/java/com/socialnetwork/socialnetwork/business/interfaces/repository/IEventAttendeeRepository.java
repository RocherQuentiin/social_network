package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.EventAttendee;
import com.socialnetwork.socialnetwork.enums.EventAttendanceStatus;

public interface IEventAttendeeRepository extends JpaRepository<EventAttendee, UUID> {

	Optional<EventAttendee> findByEvent_idAndUser_id(UUID eventID, UUID userID);

	List<EventAttendee> findBystatusAndEvent_creator_id(EventAttendanceStatus pending, UUID userID);

	List<EventAttendee> findBystatusAndUser_id(EventAttendanceStatus pending, UUID requesterId);

	List<EventAttendee> findByEvent_idAndStatus(UUID eventID, EventAttendanceStatus status);

}
