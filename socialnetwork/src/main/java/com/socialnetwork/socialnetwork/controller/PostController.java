package com.socialnetwork.socialnetwork.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.dto.PostDto;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class PostController {

    private final IPostRepository postRepository;
    private final IUserRepository userRepository;

    public PostController(IPostRepository postRepository, IUserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") UUID id) {
        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        PostDto dto = new PostDto();
        dto.setId(p.getId());
        dto.setContent(p.getContent());
        dto.setVisibilityType(p.getVisibilityType());
        dto.setAllowComments(p.getAllowComments());
        dto.setAuthorId(p.getAuthor() != null ? p.getAuthor().getId() : null);
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/post/{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") UUID id, @RequestBody PostDto body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID sessionUserId;
        try {
            sessionUserId = UUID.fromString(session.getAttribute("userId").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid session user");
        }

        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        User author = p.getAuthor();
        if (author == null || !sessionUserId.equals(author.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can edit");
        }

        if (body.getContent() != null) p.setContent(body.getContent());
        if (body.getVisibilityType() != null) p.setVisibilityType(body.getVisibilityType());
        if (body.getAllowComments() != null) p.setAllowComments(body.getAllowComments());

        // set updatedAt to Europe/Paris now
        ZonedDateTime nowParis = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        p.setUpdatedAt(LocalDateTime.of(nowParis.toLocalDate(), nowParis.toLocalTime()));

        postRepository.save(p);

        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/post/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") UUID id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID sessionUserId;
        try {
            sessionUserId = UUID.fromString(session.getAttribute("userId").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid session user");
        }

        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        User author = p.getAuthor();
        if (author == null || !sessionUserId.equals(author.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can delete");
        }

        // set deletedAt to now (Europe/Paris) and mark visibility to PRIVATE to hide
        ZonedDateTime nowParis = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        p.setDeletedAt(LocalDateTime.of(nowParis.toLocalDate(), nowParis.toLocalTime()));
        p.setVisibilityType(VisibilityType.PRIVATE);

        postRepository.save(p);
        return ResponseEntity.ok().build();
    }
}
