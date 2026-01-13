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
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.dto.ProjectDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

@Service
public class ProjectService implements IProjectService {

    private final IProjectRepository projectRepository;
    private final IProjectMemberRepository projectMemberRepository;
    private final IUserRepository userRepository;

    public ProjectService(IProjectRepository projectRepository, IProjectMemberRepository projectMemberRepository,
                         IUserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<Project> createProject(ProjectDto projectDto, UUID creatorId) {
        // Retrieve the creator user
        Optional<User> creator = userRepository.findById(creatorId);
        if (!creator.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Create a new project
        Project project = new Project();
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setVisibilityType(projectDto.getVisibility() != null ? projectDto.getVisibility() : VisibilityType.PRIVATE);
        project.setCreator(creator.get());

        // Save the project
        Project savedProject = projectRepository.save(project);

        // Add creator as OWNER member
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(creator.get());
        ownerMember.setRole(ProjectMemberRole.OWNER);
        projectMemberRepository.save(ownerMember);

        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Project> updateProject(UUID projectId, ProjectDto projectDto, UUID userId) {
        // Retrieve the project
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Verify that the user is the creator (OWNER)
        if (!project.get().getCreator().getId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Update project fields
        project.get().setName(projectDto.getName());
        project.get().setDescription(projectDto.getDescription());
        project.get().setVisibilityType(projectDto.getVisibility() != null ? projectDto.getVisibility() : VisibilityType.PRIVATE);

        // Save the updated project
        Project updatedProject = projectRepository.save(project.get());

        return new ResponseEntity<>(updatedProject, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Project> getProjectById(UUID projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(project.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Project>> getProjectsByCreator(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<Project>> projects = projectRepository.findByCreator(user.get());
        if (!projects.isPresent() || projects.get().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(projects.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Project>> getPublicProjects() {
        Optional<List<Project>> projects = projectRepository.findByVisibilityType(VisibilityType.PUBLIC);
        if (!projects.isPresent() || projects.get().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(projects.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Project>> getProjectsVisibleToUser(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<Project>> projects = projectRepository.findProjectsVisibleToUser(userId);
        if (!projects.isPresent() || projects.get().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(projects.get(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteProject(UUID projectId, UUID userId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (!project.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Verify that the user is the creator (OWNER)
        if (!project.get().getCreator().getId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        projectRepository.delete(project.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
