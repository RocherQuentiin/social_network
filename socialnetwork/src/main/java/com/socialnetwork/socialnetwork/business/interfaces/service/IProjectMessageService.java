package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.entity.ProjectMessageGroup;
import com.socialnetwork.socialnetwork.entity.ProjectMessage;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface IProjectMessageService {
    ResponseEntity<ProjectMessageGroup> createMessageGroup(Project project, String name, String description);
    ResponseEntity<List<ProjectMessageGroup>> getProjectMessageGroups(UUID projectId);
    ResponseEntity<ProjectMessage> sendProjectMessage(UUID messageGroupId, User sender, String content);
    ResponseEntity<List<ProjectMessage>> getGroupMessages(UUID messageGroupId);
    ResponseEntity<ProjectMessage> markProjectMessageAsRead(UUID messageId);
    long getUnreadCount(UUID messageGroupId);
}
