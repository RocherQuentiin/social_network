package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Post;

public interface IPostRepository extends JpaRepository<Post, UUID> {
	@Query(value = """
			  SELECT p.* FROM post p
			  WHERE p.visibility_type = 'PUBLIC'
			  OR (p.visibility_type = 'PRIVATE'
					AND p.author_id = :userID)
			  OR (p.visibility_type = 'FRIENDS'
					AND p.author_id = :userID)
			  OR (p.visibility_type = 'FRIENDS'
					AND EXISTS (
			        	SELECT 1
			        	FROM connection c
			        	WHERE c.connection_status = 'Accepted'
			          	AND (
			                (c.requester_id = p.author_id AND c.receiver_id = :userID)
			             OR (c.receiver_id = p.author_id AND c.requester_id = :userID)
			          	)
			    )
			   );
			   """, nativeQuery = true)
	List<Post> findAllPostOfUser(@Param("userID") UUID userID);

	@Query(value = """
			SELECT p.*
			FROM post p
			WHERE
			  p.visibility_type = 'PUBLIC'
			""", nativeQuery = true)
	List<Post> findByVisibilityPublic();

}
