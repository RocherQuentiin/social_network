package com.socialnetwork.socialnetwork.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMemberService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectSkillService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectRequestService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.ProjectDto;
import com.socialnetwork.socialnetwork.dto.ProjectMemberDto;
import com.socialnetwork.socialnetwork.dto.ProjectRequestDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.ProjectSkill;
import com.socialnetwork.socialnetwork.entity.ProjectRequest;
import com.socialnetwork.socialnetwork.enums.VisibilityType;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final IProjectService projectService;
    private final IProjectMemberService projectMemberService;
    private final IProjectSkillService projectSkillService;
    private final IProjectRequestService projectRequestService;

    public ProjectController(IProjectService projectService, 
                           IProjectMemberService projectMemberService,
                           IProjectSkillService projectSkillService,
                           IProjectRequestService projectRequestService) {
        this.projectService = projectService;
        this.projectMemberService = projectMemberService;
        this.projectSkillService = projectSkillService;
        this.projectRequestService = projectRequestService;
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
     * Get user's own projects (where user is owner or member)
     * GET /api/project/my-projects
     */
    @GetMapping("/my-projects")
    public ResponseEntity<?> getUserProjects(HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        return projectService.getUserProjects(userId);
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

    /**
     * Update a member's role in a project
     * PUT /api/project/{projectId}/member/{memberId}/role
     */
    @PutMapping("/{id}/member/{memberId}/role")
    public ResponseEntity<?> updateMemberRole(@PathVariable("id") UUID projectId,
                                              @PathVariable("memberId") UUID memberId,
                                              @RequestParam("role") String roleStr,
                                              HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID requesterId = UUID.fromString(userIsConnected.toString());
        
        try {
            ProjectMemberRole role = ProjectMemberRole.valueOf(roleStr);
            ResponseEntity<ProjectMember> response = projectMemberService.updateMemberRole(projectId, memberId, role, requesterId);
            return response;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role: " + roleStr);
        }
    }

    // ========== Project Skills Endpoints ==========

    /**
     * Get all skills for a project
     * GET /api/project/{projectId}/skills
     */
    @GetMapping("/{projectId}/skills")
    public ResponseEntity<List<ProjectSkill>> getProjectSkills(@PathVariable UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return projectSkillService.getProjectSkills(projectId);
    }

    /**
     * Add skills to a project (OWNER/ADMIN only)
     * POST /api/project/{projectId}/skills
     */
    @PostMapping("/{projectId}/skills")
    public ResponseEntity<?> addSkillsToProject(@PathVariable UUID projectId, 
                                               @RequestBody List<String> skills,
                                               HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Check if user is OWNER or ADMIN
        ResponseEntity<?> roleResponse = projectMemberService.getUserRoleInProject(projectId, userId);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to add skills");
        }

        return projectSkillService.addSkillsToProject(projectId, skills);
    }

    /**
     * Remove all skills from a project (OWNER only)
     * DELETE /api/project/{projectId}/skills
     */
    @DeleteMapping("/{projectId}/skills")
    public ResponseEntity<?> removeAllSkills(@PathVariable UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Check if user is OWNER
        ResponseEntity<?> roleResponse = projectMemberService.getUserRoleInProject(projectId, userId);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only owners can remove all skills");
        }

        return projectSkillService.removeAllSkills(projectId);
    }

    /**
     * Search projects by skill
     * GET /api/project/search/skill/{skillName}
     */
    @GetMapping("/search/skill/{skillName}")
    public ResponseEntity<List<Project>> searchProjectsBySkill(@PathVariable String skillName, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return projectSkillService.findProjectsBySkill(skillName);
    }

    // ========== Project Request Endpoints ==========

    /**
     * Request to join a project
     * POST /api/project/{projectId}/request
     */
    @PostMapping("/{projectId}/request")
    public ResponseEntity<?> requestToJoinProject(@PathVariable UUID projectId,
                                                 @RequestBody ProjectRequestDto requestDto,
                                                 HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<ProjectRequest> response = projectRequestService.createRequest(projectId, userId, requestDto);

        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("You have already requested to join this project or are already a member");
        }

        return response;
    }

    /**
     * Get all requests for a project (OWNER/ADMIN only)
     * GET /api/project/{projectId}/requests
     */
    @GetMapping("/{projectId}/requests")
    public ResponseEntity<?> getProjectRequests(@PathVariable UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Check if user is OWNER or ADMIN
        ResponseEntity<?> roleResponse = projectMemberService.getUserRoleInProject(projectId, userId);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view requests");
        }

        return projectRequestService.getProjectRequests(projectId);
    }

    /**
     * Get pending requests for a project (OWNER/ADMIN only)
     * GET /api/project/{projectId}/requests/pending
     */
    @GetMapping("/{projectId}/requests/pending")
    public ResponseEntity<?> getPendingRequests(@PathVariable UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

        // Check if user is OWNER or ADMIN
        ResponseEntity<?> roleResponse = projectMemberService.getUserRoleInProject(projectId, userId);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view requests");
        }

        return projectRequestService.getPendingRequests(projectId);
    }

    /**
     * Get user's own requests
     * GET /api/project/requests/user
     */
    @GetMapping("/requests/user")
    public ResponseEntity<List<ProjectRequest>> getUserRequests(HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        return projectRequestService.getUserRequests(userId);
    }

    /**
     * Accept a project request (OWNER/ADMIN only)
     * PUT /api/project/request/{requestId}/accept
     */
    @PutMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable UUID requestId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID responderId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<ProjectRequest> response = projectRequestService.acceptRequest(requestId, responderId);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to accept requests");
        }

        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This request has already been processed");
        }

        return response;
    }

    /**
     * Reject a project request (OWNER/ADMIN only)
     * PUT /api/project/request/{requestId}/reject
     */
    @PutMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable UUID requestId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID responderId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<ProjectRequest> response = projectRequestService.rejectRequest(requestId, responderId);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to reject requests");
        }

        if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This request has already been processed");
        }

        return response;
    }

    /**
     * Check if user has already requested to join
     * GET /api/project/{projectId}/has-requested
     */
    @GetMapping("/{projectId}/has-requested")
    public ResponseEntity<Boolean> hasUserRequested(@PathVariable UUID projectId, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        boolean hasRequested = projectRequestService.hasUserRequested(projectId, userId);
        return ResponseEntity.ok(hasRequested);
    }

    /**
     * Transfer ownership to another member
     * PUT /api/project/{projectId}/transfer-ownership
     */
    @PutMapping("/{projectId}/transfer-ownership")
    public ResponseEntity<?> transferOwnership(@PathVariable UUID projectId,
                                              @RequestParam UUID newOwnerId,
                                              HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        UUID currentOwnerId = UUID.fromString(userIsConnected.toString());
        ResponseEntity<Void> response = projectService.transferOwnership(projectId, newOwnerId, currentOwnerId);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the project owner can transfer ownership");
        }

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project or user not found");
        }

        if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New owner must be a project member");
        }

        return response;
    }
}
