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
            OR (p.visibility_type = 'PRIVATE'
                AND CAST(p.creator_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userId AS CHAR(36)) COLLATE utf8mb4_bin)
            OR (p.visibility_type = 'FRIENDS'
                AND CAST(p.creator_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userId AS CHAR(36)) COLLATE utf8mb4_bin)
            OR (p.visibility_type = 'FRIENDS' AND EXISTS (
                SELECT 1 FROM project_member pm
                WHERE CAST(pm.project_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(p.id AS CHAR(36)) COLLATE utf8mb4_bin
                  AND CAST(pm.user_id AS CHAR(36)) COLLATE utf8mb4_bin = CAST(:userId AS CHAR(36)) COLLATE utf8mb4_bin
            ))
            """, nativeQuery = true)
    Optional<List<Project>> findProjectsVisibleToUser(@Param("userId") UUID userId);
}
