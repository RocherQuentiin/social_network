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
					AND CAST(p.author_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userID AS CHAR(36)) COLLATE utf8mb4_bin)
			  OR (p.visibility_type = 'FRIENDS'
					AND CAST(p.author_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userID AS CHAR(36)) COLLATE utf8mb4_bin)
			  OR (p.visibility_type = 'FRIENDS'
					AND EXISTS (
			        	SELECT 1
			        	FROM connection c
			        	WHERE c.connection_status = 'ACCEPTED'
			          	AND (
			                (CAST(c.requester_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(p.author_id AS CHAR(36)) COLLATE utf8mb4_bin
			                 AND CAST(c.receiver_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userID AS CHAR(36)) COLLATE utf8mb4_bin)
			             OR (CAST(c.receiver_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(p.author_id AS CHAR(36)) COLLATE utf8mb4_bin
			                 AND CAST(c.requester_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userID AS CHAR(36)) COLLATE utf8mb4_bin)
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
