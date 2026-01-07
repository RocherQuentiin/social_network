package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.enums.ReactionType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface IReactionService {

    ResponseEntity<Void> addReaction(UUID userId, UUID postId, ReactionType type);

    ResponseEntity<Void> removeReaction(UUID userId, UUID postId, ReactionType type);

    ResponseEntity<Map<String, Object>> getSummary(UUID userId, UUID postId);
}
