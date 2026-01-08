package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.socialnetwork.socialnetwork.entity.Event;


public interface IEventRepository extends JpaRepository<Event, UUID> {

	@Query(value = """
			SELECT e.* FROM event e WHERE e.event_date > CURDATE() order by e.event_date desc
			""", 
			  nativeQuery = true)
	public Optional<List<Event>> getEventByDate();
} 
