package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPost_IdAndParentCommentIsNullOrderByCreatedAtAsc(UUID postId);
    List<Comment> findByParentComment_IdOrderByCreatedAtAsc(UUID parentCommentId);
    List<Comment> findByPost_IdOrderByCreatedAtAsc(UUID postId);
    long countByPost_Id(UUID postId);
}
