package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.enums.VisibilityType;


public interface IEventRepository extends JpaRepository<Event, UUID> {

	@Query(value = """
			SELECT e.* FROM event e WHERE e.event_date > :currentdate order by e.event_date asc
			""", 
			  nativeQuery = true)
	public Optional<List<Event>> getEventByDate(@Param("currentdate") LocalDateTime currentdate);
	
	public Optional<List<Event>> findByVisibilityType(VisibilityType visibilityType);
	
	@Query(value = """
			  SELECT e.* FROM event e
			  WHERE e.visibility_type = 'PUBLIC' and e.event_date >= :eventDate
			  OR (e.visibility_type = 'PRIVATE'
					AND e.creator_id = :userID and e.event_date >= :eventDate)
			  OR (e.visibility_type = 'FRIENDS'
					AND e.creator_id = :userID and e.event_date >= :eventDate)
			  OR (e.visibility_type = 'FRIENDS' and e.event_date >= :eventDate
					AND EXISTS (
			        	SELECT 1
			        	FROM connection c
			        	WHERE c.connection_status = 'Accepted'
			          	AND (
			                (c.requester_id = e.creator_id AND c.receiver_id = :userID)
			             OR (c.receiver_id = e.creator_id AND c.requester_id = :userID)
			          	)
			    )
			   );
			   """, nativeQuery = true)
	Optional<List<Event>> findAllEventOfUser(@Param("userID") UUID userID, @Param("eventDate") LocalDateTime eventDate);
} 
