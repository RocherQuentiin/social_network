package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.ICommentRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.ICommentService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.dto.CommentDto;
import com.socialnetwork.socialnetwork.entity.Comment;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService implements ICommentService {

    private final ICommentRepository commentRepository;
    private final IPostRepository postRepository;
    private final IUserService userService;

    public CommentService(ICommentRepository commentRepository, IPostRepository postRepository, IUserService userService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<CommentDto> createComment(UUID userId, UUID postId, String content, UUID parentCommentId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Post post = postOpt.get();

        ResponseEntity<User> userResp = userService.getUserById(userId);
        if (!userResp.getStatusCode().is2xxSuccessful() || userResp.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User author = userResp.getBody();

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);

        if (parentCommentId != null) {
            Optional<Comment> parentOpt = commentRepository.findById(parentCommentId);
            if (parentOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            comment.setParentComment(parentOpt.get());
        }

        Comment saved = commentRepository.save(comment);
        return new ResponseEntity<>(toDto(saved), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CommentDto> updateComment(UUID userId, UUID commentId, String content) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Comment comment = commentOpt.get();

        if (!comment.getAuthor().getId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        comment.setContent(content);
        Comment updated = commentRepository.save(comment);
        return new ResponseEntity<>(toDto(updated), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteComment(UUID userId, UUID commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Comment comment = commentOpt.get();

        if (!comment.getAuthor().getId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        commentRepository.delete(comment);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CommentDto>> getCommentsByPost(UUID postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Comment> topLevel = commentRepository.findByPost_IdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        List<CommentDto> dtos = topLevel.stream().map(this::toDtoWithReplies).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    private CommentDto toDtoWithReplies(Comment comment) {
        CommentDto dto = toDto(comment);
        List<Comment> replies = commentRepository.findByParentComment_IdOrderByCreatedAtAsc(comment.getId());
        dto.setReplies(replies.stream().map(this::toDtoWithReplies).collect(Collectors.toList()));
        return dto;
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setAuthorAvatar(comment.getAuthor().getProfilePictureUrl());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        return dto;
    }
}
