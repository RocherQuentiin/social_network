package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMessageService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.dto.ProjectMessageDTO;
import com.socialnetwork.socialnetwork.entity.ProjectMessage;
import com.socialnetwork.socialnetwork.entity.ProjectMessageGroup;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.entity.Project;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project-messages")
public class ProjectMessageController {
    
    private final IProjectMessageService messageService;
    private final IUserService userService;
    private final IProjectService projectService;
    
    public ProjectMessageController(IProjectMessageService messageService, 
                                   IUserService userService,
                                   IProjectService projectService) {
        this.messageService = messageService;
        this.userService = userService;
        this.projectService = projectService;
    }
    
    @PostMapping("/groups")
    public ResponseEntity<ProjectMessageGroup> createMessageGroup(
            @RequestParam UUID projectId,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        
        ResponseEntity<Project> projectResponse = projectService.getProjectById(projectId);
        if (projectResponse.getStatusCode() != HttpStatusCode.valueOf(200)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        return messageService.createMessageGroup(projectResponse.getBody(), name, description);
    }
    
    @GetMapping("/groups/{projectId}")
    public ResponseEntity<List<ProjectMessageGroup>> getProjectMessageGroups(@PathVariable UUID projectId) {
        return messageService.getProjectMessageGroups(projectId);
    }
    
    @PostMapping("/send/{messageGroupId}")
    public ResponseEntity<ProjectMessageDTO> sendMessage(
            HttpServletRequest request,
            @PathVariable UUID messageGroupId,
            @RequestParam String content) {
        
        HttpSession session = request.getSession();
        UUID senderId = UUID.fromString(session.getAttribute("userId").toString());
        
        ResponseEntity<User> sender = userService.getUserById(senderId);
        if (sender.getStatusCode() != HttpStatusCode.valueOf(200)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        ResponseEntity<ProjectMessage> message = messageService.sendProjectMessage(messageGroupId, sender.getBody(), content);
        
        if (message.getStatusCode() != HttpStatusCode.valueOf(201)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        ProjectMessage msg = message.getBody();
        ProjectMessageDTO dto = new ProjectMessageDTO(
            msg.getId(),
            msg.getMessageGroup().getId(),
            msg.getSender().getId(),
            msg.getSender().getUsername(),
            msg.getContent()
        );
        
        return new ResponseEntity<>(dto, message.getStatusCode());
    }
    
    @GetMapping("/{messageGroupId}")
    public ResponseEntity<List<ProjectMessageDTO>> getGroupMessages(@PathVariable UUID messageGroupId) {
        ResponseEntity<List<ProjectMessage>> response = messageService.getGroupMessages(messageGroupId);
        
        List<ProjectMessageDTO> dtos = response.getBody().stream()
            .map(msg -> new ProjectMessageDTO(
                msg.getId(),
                msg.getMessageGroup().getId(),
                msg.getSender().getId(),
                msg.getSender().getUsername(),
                msg.getContent()
            ))
            .collect(Collectors.toList());
        
        return new ResponseEntity<>(dtos, response.getStatusCode());
    }
    
    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID messageId) {
        messageService.markProjectMessageAsRead(messageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/{messageGroupId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable UUID messageGroupId) {
        long count = messageService.getUnreadCount(messageGroupId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
