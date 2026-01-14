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
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectSkillService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPostService;
import com.socialnetwork.socialnetwork.dto.ProjectDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

@Service
public class ProjectService implements IProjectService {

    private final IProjectRepository projectRepository;
    private final IProjectMemberRepository projectMemberRepository;
    private final IUserRepository userRepository;
    private final IProjectSkillService projectSkillService;
    private final IPostService postService;

    public ProjectService(IProjectRepository projectRepository, 
                         IProjectMemberRepository projectMemberRepository,
                         IUserRepository userRepository,
                         IProjectSkillService projectSkillService,
                         IPostService postService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.projectSkillService = projectSkillService;
        this.postService = postService;
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

        // Add skills if provided
        if (projectDto.getSkills() != null && !projectDto.getSkills().isEmpty()) {
            projectSkillService.addSkillsToProject(savedProject.getId(), projectDto.getSkills());
            
            // Create a post for public projects with skills
            if (savedProject.getVisibilityType() == VisibilityType.PUBLIC) {
                createRecruitmentPost(savedProject, projectDto.getSkills(), creator.get());
            }
        }

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
    public ResponseEntity<List<Project>> getUserProjects(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Get projects where user is creator (OWNER)
        Optional<List<Project>> createdProjects = projectRepository.findByCreator(user.get());
        
        // Get projects where user is a member
        Optional<List<ProjectMember>> memberProjects = projectMemberRepository.findByUser(user.get());
        
        List<Project> allProjects = new java.util.ArrayList<>();
        
        if (createdProjects.isPresent()) {
            allProjects.addAll(createdProjects.get());
        }
        
        if (memberProjects.isPresent()) {
            // Add member projects that are not already added (to avoid duplicates)
            for (ProjectMember member : memberProjects.get()) {
                if (member.getProject() != null && !allProjects.contains(member.getProject())) {
                    allProjects.add(member.getProject());
                }
            }
        }

        if (allProjects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // Sort by updatedAt descending (most recent first), fallback to createdAt
        allProjects.sort((a, b) -> {
            java.time.LocalDateTime dateA = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
            java.time.LocalDateTime dateB = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
            return dateB.compareTo(dateA);
        });

        return new ResponseEntity<>(allProjects, HttpStatus.OK);
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
    
    /**
     * Create a recruitment post for a public project with skills
     */
    private void createRecruitmentPost(Project project, List<String> skills, User creator) {
        try {
            StringBuilder postContent = new StringBuilder();
            postContent.append("🚀 Nouveau projet : ").append(project.getName()).append("\n\n");
            
            if (project.getDescription() != null && !project.getDescription().isEmpty()) {
                postContent.append(project.getDescription()).append("\n\n");
            }
            
            postContent.append("🔍 Compétences recherchées :\n");
            for (String skill : skills) {
                postContent.append("• ").append(skill).append("\n");
            }
            
            postContent.append("\n💼 Intéressé(e) ? Consultez mon profil pour voir le projet et demander à rejoindre !");
            
            Post post = new Post();
            post.setAuthor(creator);
            post.setContent(postContent.toString());
            post.setVisibilityType(VisibilityType.PUBLIC);
            post.setAllowComments(true);
            
            postService.createPost(post);
        } catch (Exception e) {
            // Log error but don't fail project creation
            System.err.println("Failed to create recruitment post: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> transferOwnership(UUID projectId, UUID newOwnerId, UUID currentOwnerId) {
        // Get the project
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();

        // Verify current user is allowed: project creator OR has OWNER role in membership
        boolean isCreator = project.getCreator() != null && project.getCreator().getId().equals(currentOwnerId);
        boolean isOwnerRole = false;
        Optional<User> currentUserOpt = userRepository.findById(currentOwnerId);
        if (currentUserOpt.isPresent()) {
            Optional<ProjectMember> currentMembership = projectMemberRepository.findByProjectAndUser(project, currentUserOpt.get());
            isOwnerRole = currentMembership.isPresent() && currentMembership.get().getRole() == ProjectMemberRole.OWNER;
        }

        if (!isCreator && !isOwnerRole) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Get the new owner
        Optional<User> newOwnerOpt = userRepository.findById(newOwnerId);
        if (!newOwnerOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Verify new owner is a member of the project
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectAndUser(project, newOwnerOpt.get());
        if (!memberOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // User is not a member
        }

        // Update project creator
        project.setCreator(newOwnerOpt.get());
        projectRepository.save(project);

        // Update membership roles
        // Get current owner's membership and downgrade if exists
        Optional<User> currentUserOpt2 = userRepository.findById(currentOwnerId);
        if (currentUserOpt2.isPresent()) {
            Optional<ProjectMember> currentOwnerMembership = projectMemberRepository.findByProjectAndUser(project, currentUserOpt2.get());
            if (currentOwnerMembership.isPresent()) {
                // Downgrade from OWNER to MEMBER
                currentOwnerMembership.get().setRole(ProjectMemberRole.MEMBER);
                projectMemberRepository.save(currentOwnerMembership.get());
            }
        }

        // Make sure new owner has OWNER role in ProjectMember
        ProjectMember newOwnerMembership = memberOpt.get();
        newOwnerMembership.setRole(ProjectMemberRole.OWNER);
        projectMemberRepository.save(newOwnerMembership);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
