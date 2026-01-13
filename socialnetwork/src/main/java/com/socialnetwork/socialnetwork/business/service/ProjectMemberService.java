package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectMemberRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMemberService;
import com.socialnetwork.socialnetwork.dto.ProjectMemberDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;

@Service
public class ProjectMemberService implements IProjectMemberService {

    private final IProjectMemberRepository projectMemberRepository;
    private final IProjectRepository projectRepository;
    private final IUserRepository userRepository;

    public ProjectMemberService(IProjectMemberRepository projectMemberRepository,
                              IProjectRepository projectRepository,
                              IUserRepository userRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<ProjectMember> addMemberToProject(UUID projectId, ProjectMemberDto memberDto, UUID requesterId) {
        // Retrieve the project
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Retrieve the requester
        Optional<User> requester = userRepository.findById(requesterId);
        if (!requester.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if requester has permission (OWNER or ADMIN)
        Optional<ProjectMember> requesterMembership = projectMemberRepository.findByProjectAndUser(project.get(), requester.get());
        
        if (!requesterMembership.isPresent()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ProjectMemberRole requesterRole = requesterMembership.get().getRole();
        if (requesterRole != ProjectMemberRole.OWNER && requesterRole != ProjectMemberRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Retrieve the user to add
        Optional<User> userToAdd = userRepository.findById(memberDto.getUserId());
        if (!userToAdd.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is already a member
        Optional<ProjectMember> existingMember = projectMemberRepository.findByProjectAndUser(project.get(), userToAdd.get());
        if (existingMember.isPresent()) {
            // Return 409 Conflict - User already exists in project
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Create new project member
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project.get());
        newMember.setUser(userToAdd.get());
        newMember.setRole(memberDto.getRole() != null ? memberDto.getRole() : ProjectMemberRole.MEMBER);

        // Save the new member
        ProjectMember savedMember = projectMemberRepository.save(newMember);

        return new ResponseEntity<>(savedMember, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<ProjectMember>> getProjectMembers(UUID projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<ProjectMember>> members = projectMemberRepository.findByProject(project.get());
        if (!members.isPresent() || members.get().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(members.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> removeMemberFromProject(UUID projectId, UUID memberId, UUID requesterId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> memberUser = userRepository.findById(memberId);
        if (!memberUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if requester has permission (OWNER or ADMIN)
        Optional<User> requester = userRepository.findById(requesterId);
        if (!requester.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<ProjectMember> requesterMembership = projectMemberRepository.findByProjectAndUser(project.get(), requester.get());
        if (!requesterMembership.isPresent()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ProjectMemberRole requesterRole = requesterMembership.get().getRole();
        if (requesterRole != ProjectMemberRole.OWNER && requesterRole != ProjectMemberRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Prevent removing the owner
        Optional<ProjectMember> memberToRemove = projectMemberRepository.findByProjectAndUser(project.get(), memberUser.get());
        if (!memberToRemove.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (memberToRemove.get().getRole() == ProjectMemberRole.OWNER) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        projectMemberRepository.delete(memberToRemove.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> leaveProject(UUID projectId, UUID userId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<ProjectMember> membership = projectMemberRepository.findByProjectAndUser(project.get(), user.get());
        if (!membership.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Prevent owner from leaving
        if (membership.get().getRole() == ProjectMemberRole.OWNER) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        projectMemberRepository.delete(membership.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteMember(UUID projectId, UUID memberId, UUID requesterId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> memberUser = userRepository.findById(memberId);
        if (!memberUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> requester = userRepository.findById(requesterId);
        if (!requester.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Get requester's role
        Optional<ProjectMember> requesterMembership = projectMemberRepository.findByProjectAndUser(project.get(), requester.get());
        if (!requesterMembership.isPresent()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ProjectMemberRole requesterRole = requesterMembership.get().getRole();

        // Get member's role
        Optional<ProjectMember> memberToDelete = projectMemberRepository.findByProjectAndUser(project.get(), memberUser.get());
        if (!memberToDelete.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ProjectMemberRole memberRole = memberToDelete.get().getRole();

        // Check authorization based on roles
        // OWNER can delete ADMIN and MEMBER
        // ADMIN can delete MEMBER
        // ADMIN cannot delete ADMIN or OWNER
        // MEMBER cannot delete anyone

        if (requesterRole == ProjectMemberRole.OWNER) {
            // OWNER can delete anyone except themselves
            if (memberRole == ProjectMemberRole.OWNER && !requester.get().getId().equals(memberUser.get().getId())) {
                // Can delete other owners
            } else if (requester.get().getId().equals(memberUser.get().getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else if (requesterRole == ProjectMemberRole.ADMIN) {
            // ADMIN can only delete MEMBER
            if (memberRole != ProjectMemberRole.MEMBER) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            // MEMBER cannot delete anyone
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        projectMemberRepository.delete(memberToDelete.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ProjectMemberRole> getUserRoleInProject(UUID projectId, UUID userId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<ProjectMember> membership = projectMemberRepository.findByProjectAndUser(project.get(), user.get());
        if (!membership.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(membership.get().getRole(), HttpStatus.OK);
    }

    @Override
    public boolean isMemberOfProject(UUID projectId, UUID userId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return false;
        }

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return false;
        }

        Optional<ProjectMember> membership = projectMemberRepository.findByProjectAndUser(project.get(), user.get());
        return membership.isPresent();
    }
}
