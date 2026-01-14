package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IConversationRepository extends JpaRepository<Conversation, UUID> {
    
    @Query("SELECT c FROM Conversation c WHERE (c.participant1.id = :userId1 AND c.participant2.id = :userId2) OR (c.participant1.id = :userId2 AND c.participant2.id = :userId1)")
    Optional<Conversation> findConversationBetweenUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
    
    @Query("SELECT c FROM Conversation c WHERE c.participant1.id = :userId OR c.participant2.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findConversationsByUserId(@Param("userId") UUID userId);
}
