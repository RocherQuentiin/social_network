package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.dto.ProjectRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectMemberDto;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRequestRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMemberService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectRequestService;
import com.socialnetwork.socialnetwork.entity.*;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;
import com.socialnetwork.socialnetwork.enums.ProjectRequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectRequestService implements IProjectRequestService {

    private final IProjectRequestRepository projectRequestRepository;
    private final IProjectRepository projectRepository;
    private final IUserRepository userRepository;
    private final IProjectMemberService projectMemberService;

    public ProjectRequestService(IProjectRequestRepository projectRequestRepository,
                                IProjectRepository projectRepository,
                                IUserRepository userRepository,
                                IProjectMemberService projectMemberService) {
        this.projectRequestRepository = projectRequestRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberService = projectMemberService;
    }

    @Override
    @Transactional
    public ResponseEntity<ProjectRequest> createRequest(UUID projectId, UUID userId, ProjectRequestDto requestDto) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        User user = userOpt.get();

        // Vérifier si l'utilisateur est déjà membre
        boolean isMember = projectMemberService.isMemberOfProject(projectId, userId);
        if (isMember) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Déjà membre
        }

        // Vérifier si une demande existe déjà
        if (projectRequestRepository.existsByProjectAndUser(project, user)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Demande déjà envoyée
        }

        ProjectRequest request = new ProjectRequest();
        request.setProject(project);
        request.setUser(user);
        // skillName is optional from UI; persist empty string to satisfy NOT NULL
        String skill = requestDto.getSkillName();
        request.setSkillName((skill == null) ? "" : skill);
        request.setMessage(requestDto.getMessage());
        request.setStatus(ProjectRequestStatus.PENDING);

        ProjectRequest savedRequest = projectRequestRepository.save(request);
        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<ProjectRequest>> getProjectRequests(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<ProjectRequest>> requests = projectRequestRepository.findByProject(projectOpt.get());
        return new ResponseEntity<>(requests.orElse(new ArrayList<>()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProjectRequest>> getPendingRequests(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<ProjectRequest>> requests = projectRequestRepository.findByProjectAndStatus(
            projectOpt.get(), 
            ProjectRequestStatus.PENDING
        );
        return new ResponseEntity<>(requests.orElse(new ArrayList<>()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProjectRequest>> getUserRequests(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<List<ProjectRequest>> requests = projectRequestRepository.findByUser(userOpt.get());
        return new ResponseEntity<>(requests.orElse(new ArrayList<>()), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<ProjectRequest> acceptRequest(UUID requestId, UUID responderId) {
        Optional<ProjectRequest> requestOpt = projectRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> responderOpt = userRepository.findById(responderId);
        if (!responderOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ProjectRequest request = requestOpt.get();

        // Vérifier que le répondeur a le droit d'accepter (OWNER ou ADMIN)
        ResponseEntity<ProjectMemberRole> roleResponse = projectMemberService.getUserRoleInProject(
            request.getProject().getId(), 
            responderId
        );
        
        if (roleResponse.getStatusCode() != HttpStatus.OK || 
            roleResponse.getBody() == null ||
            roleResponse.getBody() == ProjectMemberRole.MEMBER) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Vérifier que la demande est en attente
        if (request.getStatus() != ProjectRequestStatus.PENDING) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Ajouter l'utilisateur au projet
        ProjectMemberDto memberDto = new ProjectMemberDto();
        memberDto.setUserId(request.getUser().getId());
        memberDto.setRole(ProjectMemberRole.MEMBER);
        
        ResponseEntity<?> addMemberResponse = projectMemberService.addMemberToProject(
            request.getProject().getId(),
            memberDto,
            responderId
        );

        if (addMemberResponse.getStatusCode() != HttpStatus.CREATED) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Mettre à jour la demande
        request.setStatus(ProjectRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(responderOpt.get());

        ProjectRequest updatedRequest = projectRequestRepository.save(request);
        return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<ProjectRequest> rejectRequest(UUID requestId, UUID responderId) {
        Optional<ProjectRequest> requestOpt = projectRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> responderOpt = userRepository.findById(responderId);
        if (!responderOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ProjectRequest request = requestOpt.get();

        // Vérifier que le répondeur a le droit de rejeter (OWNER ou ADMIN)
        ResponseEntity<ProjectMemberRole> roleResponse = projectMemberService.getUserRoleInProject(
            request.getProject().getId(), 
            responderId
        );
        
        if (roleResponse.getStatusCode() != HttpStatus.OK || 
            roleResponse.getBody() == null ||
            roleResponse.getBody() == ProjectMemberRole.MEMBER) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Vérifier que la demande est en attente
        if (request.getStatus() != ProjectRequestStatus.PENDING) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Mettre à jour la demande
        request.setStatus(ProjectRequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(responderOpt.get());

        ProjectRequest updatedRequest = projectRequestRepository.save(request);
        return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
    }

    @Override
    public boolean hasUserRequested(UUID projectId, UUID userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }

        return projectRequestRepository.existsByProjectAndUser(projectOpt.get(), userOpt.get());
    }
}
