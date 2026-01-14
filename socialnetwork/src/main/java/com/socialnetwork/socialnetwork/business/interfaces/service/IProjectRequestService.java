package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.dto.ProjectRequestDto;
import com.socialnetwork.socialnetwork.entity.ProjectRequest;
import com.socialnetwork.socialnetwork.enums.ProjectRequestStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface IProjectRequestService {
    
    /**
     * Create a request to join a project
     */
    ResponseEntity<ProjectRequest> createRequest(UUID projectId, UUID userId, ProjectRequestDto requestDto);
    
    /**
     * Get all requests for a project
     */
    ResponseEntity<List<ProjectRequest>> getProjectRequests(UUID projectId);
    
    /**
     * Get pending requests for a project
     */
    ResponseEntity<List<ProjectRequest>> getPendingRequests(UUID projectId);
    
    /**
     * Get all requests from a user
     */
    ResponseEntity<List<ProjectRequest>> getUserRequests(UUID userId);
    
    /**
     * Accept a request
     */
    ResponseEntity<ProjectRequest> acceptRequest(UUID requestId, UUID responderId);
    
    /**
     * Reject a request
     */
    ResponseEntity<ProjectRequest> rejectRequest(UUID requestId, UUID responderId);
    
    /**
     * Check if user has already requested
     */
    boolean hasUserRequested(UUID projectId, UUID userId);
}
