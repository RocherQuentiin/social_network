package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Reaction;
import com.socialnetwork.socialnetwork.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUser_IdAndPost_IdAndReactionType(UUID userId, UUID postId, ReactionType reactionType);

    long countByPost_IdAndReactionType(UUID postId, ReactionType reactionType);

    List<Reaction> findByPost_Id(UUID postId);
}
