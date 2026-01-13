package com.socialnetwork.socialnetwork.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMemberService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.ProjectDto;
import com.socialnetwork.socialnetwork.dto.ProjectMemberDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final IProjectService projectService;
    private final IProjectMemberService projectMemberService;

    public ProjectController(IProjectService projectService, IProjectMemberService projectMemberService) {
        this.projectService = projectService;
        this.projectMemberService = projectMemberService;
    }

    /**
     * Create a new project
     * POST /api/project/
     */
    @PostMapping("/")
    public ResponseEntity<?> createProject(@RequestBody ProjectDto projectDto, HttpServletRequest request) {
        // Get the authenticated user ID
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Validate input
        if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Project name is required");
        }

        // Create the project
        ResponseEntity<Project> response = projectService.createProject(projectDto, userId);

        // If project was created, add creator as OWNER member
        if (response.getStatusCode() == HttpStatusCode.valueOf(201)) {
            Project project = response.getBody();
            // The creator is automatically added as OWNER by the service or needs to be added here
            // This might be added in the ProjectService.createProject method
        }

        return response;
    }

    /**
     * Update an existing project
     * PUT /api/project/{projectId}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable("id") UUID projectId, 
                                          @RequestBody ProjectDto projectDto,
                                          HttpServletRequest request) {
        // Get the authenticated user ID
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Validate input
        if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Project name is required");
        }

        // Update the project
        ResponseEntity<Project> response = projectService.updateProject(projectId, projectDto, userId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only project owner or admins can modify the project");
        }

        return response;
    }

    /**
     * Get a project by ID
     * GET /api/project/{projectId}
     */
    @GetMapping("{id}")
    public ResponseEntity<?> getProject(@PathVariable("id") UUID projectId) {
        return projectService.getProjectById(projectId);
    }

    /**
     * Get all public projects
     * GET /api/project/public
     */
    @GetMapping("/public")
    public ResponseEntity<?> getPublicProjects() {
        return projectService.getPublicProjects();
    }

    /**
     * Get projects visible to a user
     * GET /api/project/user/{userId}
     */
    @GetMapping("user/{userId}")
    public ResponseEntity<?> getProjectsVisibleToUser(@PathVariable("userId") UUID userId) {
        return projectService.getProjectsVisibleToUser(userId);
    }

    /**     * Get public projects created by a specific user
     * GET /api/project/creator/{userId}/public
     */
    @GetMapping("/creator/{userId}/public")
    public ResponseEntity<?> getPublicProjectsByCreator(@PathVariable("userId") UUID creatorId) {
        ResponseEntity<List<Project>> allProjects = projectService.getProjectsByCreator(creatorId);
        
        if (allProjects.getBody() == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Filter to keep only public projects
        List<Project> publicProjects = allProjects.getBody().stream()
            .filter(project -> project.getVisibilityType() == VisibilityType.PUBLIC)
            .toList();
        
        return ResponseEntity.ok(publicProjects);
    }

    /**     * Delete a project
     * DELETE /api/project/{projectId}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable("id") UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<Void> response = projectService.deleteProject(projectId, userId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only project owner can delete the project");
        }

        return response;
    }

    // ==================== Project Member Methods ====================

    /**
     * Add a member to a project
     * POST /api/project/{projectId}/member
     */
    @PostMapping("/{id}/member")
    public ResponseEntity<?> addMemberToProject(@PathVariable("id") UUID projectId,
                                               @RequestBody ProjectMemberDto memberDto,
                                               HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID requesterId = UUID.fromString(userIsConnected.toString());

        // Validate input
        if (memberDto.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID is required");
        }

        ResponseEntity<ProjectMember> response = projectMemberService.addMemberToProject(projectId, memberDto, requesterId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(409)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists in the project");
        }

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only project owner or admins can add members");
        }

        return response;
    }

    /**
     * Get all members of a project
     * GET /api/project/{projectId}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable("id") UUID projectId) {
        return projectMemberService.getProjectMembers(projectId);
    }

    /**
     * Remove a member from a project
     * DELETE /api/project/{projectId}/member/{memberId}
     */
    @DeleteMapping("/{id}/member/{memberId}")
    public ResponseEntity<?> removeMemberFromProject(@PathVariable("id") UUID projectId,
                                                     @PathVariable("memberId") UUID memberId,
                                                     HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID requesterId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<Void> response = projectMemberService.removeMemberFromProject(projectId, memberId, requesterId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only project owner or admins can remove members");
        }

        return response;
    }

    /**
     * Leave a project (user removes themselves)
     * DELETE /api/project/{projectId}/leave
     */
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leaveProject(@PathVariable("id") UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<Void> response = projectMemberService.leaveProject(projectId, userId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Project owner cannot leave the project");
        }

        return response;
    }

    /**
     * Get user's role in a project
     * GET /api/project/{projectId}/user-role
     */
    @GetMapping("/{id}/user-role")
    public ResponseEntity<?> getUserRoleInProject(@PathVariable("id") UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<com.socialnetwork.socialnetwork.enums.ProjectMemberRole> response = projectMemberService.getUserRoleInProject(projectId, userId);

        return response;
    }

    /**
     * Delete a member from a project with role-based authorization
     * POST /api/project/{projectId}/member/{memberId}/delete
     */
    @PostMapping("/{id}/member/{memberId}/delete")
    public ResponseEntity<?> deleteMember(@PathVariable("id") UUID projectId,
                                         @PathVariable("memberId") UUID memberId,
                                         HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID requesterId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<Void> response = projectMemberService.deleteMember(projectId, memberId, requesterId);

        if (response.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This deletion is not allowed");
        }

        return response;
    }
}
