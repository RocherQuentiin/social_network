package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.dto.ProjectDto;
import com.socialnetwork.socialnetwork.entity.Project;

public interface IProjectService {

    /**
     * Create a new project
     */
    ResponseEntity<Project> createProject(ProjectDto projectDto, UUID creatorId);

    /**
     * Update an existing project
     */
    ResponseEntity<Project> updateProject(UUID projectId, ProjectDto projectDto, UUID userId);

    /**
     * Get a project by ID
     */
    ResponseEntity<Project> getProjectById(UUID projectId);

    /**
     * Get all projects created by a user
     */
    ResponseEntity<List<Project>> getProjectsByCreator(UUID userId);

    /**
     * Get user's own projects (where user is owner or member)
     */
    ResponseEntity<List<Project>> getUserProjects(UUID userId);

    /**
     * Get all public projects
     */
    ResponseEntity<List<Project>> getPublicProjects();

    /**
     * Get projects visible to a user
     */
    ResponseEntity<List<Project>> getProjectsVisibleToUser(UUID userId);

    /**
     * Delete a project
     */
    ResponseEntity<Void> deleteProject(UUID projectId, UUID userId);
    
    /**
     * Transfer ownership to another member
     */
    ResponseEntity<Void> transferOwnership(UUID projectId, UUID newOwnerId, UUID currentOwnerId);
}
