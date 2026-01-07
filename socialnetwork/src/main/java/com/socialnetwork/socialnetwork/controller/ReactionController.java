package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.IReactionService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.enums.ReactionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reactions")
public class ReactionController {

    private final IReactionService reactionService;

    public ReactionController(IReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping
    public ResponseEntity<?> addReaction(HttpServletRequest request,
                                         @RequestParam("postId") String postId,
                                         @RequestParam("type") String type) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());
        UUID postUuid = UUID.fromString(postId);
        ReactionType rt;
        try {
            rt = ReactionType.valueOf(type);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid reaction type");
        }
        return reactionService.addReaction(userId, postUuid, rt);
    }

    @DeleteMapping
    public ResponseEntity<?> removeReaction(HttpServletRequest request,
                                            @RequestParam("postId") String postId,
                                            @RequestParam("type") String type) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());
        UUID postUuid = UUID.fromString(postId);
        ReactionType rt;
        try {
            rt = ReactionType.valueOf(type);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid reaction type");
        }
        return reactionService.removeReaction(userId, postUuid, rt);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpServletRequest request,
                                        @RequestParam("postId") String postId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());
        UUID postUuid = UUID.fromString(postId);
        ResponseEntity<Map<String, Object>> resp = reactionService.getSummary(userId, postUuid);
        return resp;
    }
}
