package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Event;


public interface IEventRepository extends JpaRepository<Event, UUID> {

	@Query(value = """
			SELECT e.* FROM event e WHERE e.event_date > :currentdate order by e.event_date asc
			""", 
			  nativeQuery = true)
	public Optional<List<Event>> getEventByDate(@Param("currentdate") LocalDateTime currentdate);
} 
