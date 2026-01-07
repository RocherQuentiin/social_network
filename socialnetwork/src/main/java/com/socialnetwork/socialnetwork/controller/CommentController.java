package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.ICommentService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.CommentDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> createComment(HttpServletRequest request,
                                           @RequestBody Map<String, String> body) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());

        String postIdStr = body.get("postId");
        String content = body.get("content");
        String parentCommentIdStr = body.get("parentCommentId");

        if (postIdStr == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing postId or content");
        }

        UUID postId = UUID.fromString(postIdStr);
        UUID parentCommentId = (parentCommentIdStr != null && !parentCommentIdStr.isEmpty()) ? UUID.fromString(parentCommentIdStr) : null;

        return commentService.createComment(userId, postId, content, parentCommentId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(HttpServletRequest request,
                                           @PathVariable("id") String commentId,
                                           @RequestBody Map<String, String> body) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());
        UUID commentUuid = UUID.fromString(commentId);

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing content");
        }

        return commentService.updateComment(userId, commentUuid, content);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(HttpServletRequest request,
                                           @PathVariable("id") String commentId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID userId = UUID.fromString(userIsConnect.toString());
        UUID commentUuid = UUID.fromString(commentId);

        return commentService.deleteComment(userId, commentUuid);
    }

    @GetMapping
    public ResponseEntity<?> getComments(HttpServletRequest request,
                                         @RequestParam("postId") String postId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID postUuid = UUID.fromString(postId);
        ResponseEntity<List<CommentDto>> resp = commentService.getCommentsByPost(postUuid);
        return resp;
    }
}
