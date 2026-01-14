package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectRequest;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ProjectRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProjectRequestRepository extends JpaRepository<ProjectRequest, UUID> {

    /**
     * Find all requests for a project
     */
    Optional<List<ProjectRequest>> findByProject(Project project);

    /**
     * Find all requests from a user
     */
    Optional<List<ProjectRequest>> findByUser(User user);

    /**
     * Find a specific request
     */
    Optional<ProjectRequest> findByProjectAndUser(Project project, User user);

    /**
     * Find requests by status
     */
    Optional<List<ProjectRequest>> findByProjectAndStatus(Project project, ProjectRequestStatus status);

    /**
     * Check if user already has a request for this project
     */
    boolean existsByProjectAndUser(Project project, User user);
}
