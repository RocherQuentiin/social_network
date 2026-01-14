package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Connection;

public interface IConnectionService {
    ResponseEntity<Connection> sendRequest(UUID requesterId, UUID receiverId);

    ResponseEntity<Connection> acceptRequest(UUID receiverId, UUID requesterId);

    ResponseEntity<Void> declineRequest(UUID receiverId, UUID requesterId);

    ResponseEntity<List<Connection>> getPendingFor(UUID receiverId);

    ResponseEntity<List<Connection>> getSentRequestsFor(UUID requesterId);

	List<Connection> findAllAcceptedRequestByUserID(UUID requesterId);
}
