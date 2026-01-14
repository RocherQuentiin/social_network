package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.dto.ProjectMemberDto;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;

public interface IProjectMemberService {

    /**
     * Add a member to a project
     */
    ResponseEntity<ProjectMember> addMemberToProject(UUID projectId, ProjectMemberDto memberDto, UUID requesterId);

    /**
     * Get all members of a project
     */
    ResponseEntity<List<ProjectMember>> getProjectMembers(UUID projectId);

    /**
     * Remove a member from a project
     */
    ResponseEntity<Void> removeMemberFromProject(UUID projectId, UUID memberId, UUID requesterId);

    /**
     * Leave a project (a user removes themselves)
     */
    ResponseEntity<Void> leaveProject(UUID projectId, UUID userId);

    /**
     * Delete a member from a project with role-based authorization
     */
    ResponseEntity<Void> deleteMember(UUID projectId, UUID memberId, UUID requesterId);

    /**
     * Get user's role in a project
     */
    ResponseEntity<ProjectMemberRole> getUserRoleInProject(UUID projectId, UUID userId);

    /**
     * Check if user is member of project
     */
    boolean isMemberOfProject(UUID projectId, UUID userId);

    /**
     * Update a member's role in a project
     */
    ResponseEntity<ProjectMember> updateMemberRole(UUID projectId, UUID memberId, ProjectMemberRole newRole, UUID requesterId);
}
