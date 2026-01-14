package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

public interface IProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find all projects created by a user
     */
    Optional<List<Project>> findByCreator(User creator);

    /**
     * Find all public projects
     */
    Optional<List<Project>> findByVisibilityType(VisibilityType visibilityType);

    /**
     * Find projects visible to a user (PUBLIC, PRIVATE owned by user, or FRIENDS if connected)
     */
    @Query(value = """
            SELECT p.* FROM project p
            WHERE p.visibility_type = 'PUBLIC'
            OR (p.visibility_type = 'PRIVATE' AND p.creator_id = :userId)
            OR (p.visibility_type = 'FRIENDS' AND p.creator_id = :userId)
            OR (p.visibility_type = 'FRIENDS' AND EXISTS (
                SELECT 1 FROM project_member pm 
                WHERE pm.project_id = p.id AND pm.user_id = :userId
            ))
            """, nativeQuery = true)
    Optional<List<Project>> findProjectsVisibleToUser(@Param("userId") UUID userId);
}
