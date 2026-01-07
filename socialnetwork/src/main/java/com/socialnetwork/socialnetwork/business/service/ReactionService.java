package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IReactionRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IReactionService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.Reaction;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ReactionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReactionService implements IReactionService {

    private final IReactionRepository reactionRepository;
    private final IPostRepository postRepository;
    private final IUserService userService;

    public ReactionService(IReactionRepository reactionRepository, IPostRepository postRepository, IUserService userService) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<Void> addReaction(UUID userId, UUID postId, ReactionType type) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Prevent duplicate for same type/user/post
        Optional<Reaction> existing = reactionRepository.findByUser_IdAndPost_IdAndReactionType(userId, postId, type);
        if (existing.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        ResponseEntity<User> userResp = userService.getUserById(userId);
        if (!userResp.getStatusCode().is2xxSuccessful() || userResp.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Reaction r = new Reaction();
        r.setPost(postOpt.get());
        r.setUser(userResp.getBody());
        r.setReactionType(type);

        reactionRepository.save(r);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> removeReaction(UUID userId, UUID postId, ReactionType type) {
        Optional<Reaction> existing = reactionRepository.findByUser_IdAndPost_IdAndReactionType(userId, postId, type);
        if (existing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        reactionRepository.delete(existing.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getSummary(UUID userId, UUID postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Long> counts = new LinkedHashMap<>();
        for (ReactionType t : ReactionType.values()) {
            counts.put(t.name(), reactionRepository.countByPost_IdAndReactionType(postId, t));
        }

        List<Reaction> reactions = reactionRepository.findByPost_Id(postId);
        Map<String, List<String>> usersByType = new LinkedHashMap<>();
        Set<String> userTypes = new HashSet<>();
        for (ReactionType t : ReactionType.values()) {
            usersByType.put(t.name(), new ArrayList<>());
        }

        for (Reaction r : reactions) {
            if (r.getUser() != null && r.getReactionType() != null) {
                String uname = r.getUser().getUsername() != null ? r.getUser().getUsername() : r.getUser().getEmail();
                usersByType.get(r.getReactionType().name()).add(uname);
                if (r.getUser().getId() != null && r.getUser().getId().equals(userId)) {
                    userTypes.add(r.getReactionType().name());
                }
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("counts", counts);
        payload.put("usersByType", usersByType);
        payload.put("userReactedTypes", userTypes);
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        payload.put("total", total);

        return new ResponseEntity<>(payload, HttpStatus.OK);
    }
}
