package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.dto.CommentDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    ResponseEntity<CommentDto> createComment(UUID userId, UUID postId, String content, UUID parentCommentId);
    ResponseEntity<CommentDto> updateComment(UUID userId, UUID commentId, String content);
    ResponseEntity<Void> deleteComment(UUID userId, UUID commentId);
    ResponseEntity<List<CommentDto>> getCommentsByPost(UUID postId);
}
