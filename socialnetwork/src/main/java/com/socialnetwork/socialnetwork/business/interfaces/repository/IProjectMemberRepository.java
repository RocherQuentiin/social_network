package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;

public interface IProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    /**
     * Find all members of a project
     */
    Optional<List<ProjectMember>> findByProject(Project project);

    /**
     * Find a specific project member by project and user
     */
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    /**
     * Find all projects a user is member of
     */
    Optional<List<ProjectMember>> findByUser(User user);

    /**
     * Check if a user is member of a project
     */
    @Query(value = """
            SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END 
            FROM project_member pm 
            WHERE pm.project_id = :projectId AND pm.user_id = :userId
            """, nativeQuery = true)
    boolean isMemberOfProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Get the role of a user in a project
     */
    @Query(value = """
            SELECT pm.project_member_role 
            FROM project_member pm 
            WHERE pm.project_id = :projectId AND pm.user_id = :userId
            """, nativeQuery = true)
    Optional<String> getUserRoleInProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Delete a project member by project and user
     */
    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM project_member 
            WHERE project_id = :projectId AND user_id = :userId
            """, nativeQuery = true)
    void deleteByProjectAndUser(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
