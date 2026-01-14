package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectMessageGroupRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectMessageRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectMessageService;
import com.socialnetwork.socialnetwork.entity.ProjectMessageGroup;
import com.socialnetwork.socialnetwork.entity.ProjectMessage;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectMessageService implements IProjectMessageService {
    
    private final IProjectMessageGroupRepository messageGroupRepository;
    private final IProjectMessageRepository messageRepository;
    
    public ProjectMessageService(IProjectMessageGroupRepository messageGroupRepository, 
                                  IProjectMessageRepository messageRepository) {
        this.messageGroupRepository = messageGroupRepository;
        this.messageRepository = messageRepository;
    }
    
    @Override
    public ResponseEntity<ProjectMessageGroup> createMessageGroup(Project project, String name, String description) {
        ProjectMessageGroup group = new ProjectMessageGroup(project, name);
        group.setDescription(description);
        
        ProjectMessageGroup saved = messageGroupRepository.save(group);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    @Override
    public ResponseEntity<List<ProjectMessageGroup>> getProjectMessageGroups(UUID projectId) {
        List<ProjectMessageGroup> groups = messageGroupRepository.findByProjectIdOrderedByCreation(projectId);
        return new ResponseEntity<>(groups, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<ProjectMessage> sendProjectMessage(UUID messageGroupId, User sender, String content) {
        Optional<ProjectMessageGroup> group = messageGroupRepository.findById(messageGroupId);
        
        if (group.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        ProjectMessage message = new ProjectMessage(group.get(), sender, content);
        ProjectMessage saved = messageRepository.save(message);
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    @Override
    public ResponseEntity<List<ProjectMessage>> getGroupMessages(UUID messageGroupId) {
        List<ProjectMessage> messages = messageRepository.findByMessageGroupIdOrderByCreatedAtAsc(messageGroupId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<ProjectMessage> markProjectMessageAsRead(UUID messageId) {
        Optional<ProjectMessage> message = messageRepository.findById(messageId);
        
        if (message.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        message.get().setIsRead(true);
        ProjectMessage saved = messageRepository.save(message.get());
        
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }
    
    @Override
    public long getUnreadCount(UUID messageGroupId) {
        return messageRepository.countUnreadByMessageGroupId(messageGroupId);
    }
}
