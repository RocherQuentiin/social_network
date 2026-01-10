package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.EventAttendee;

public interface IEventAttendeeRepository extends JpaRepository<EventAttendee, UUID> {

	Optional<EventAttendee> findByidAndUser_id(UUID eventID, UUID userID);

}
