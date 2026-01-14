package com.socialnetwork.socialnetwork.business.interfaces.repository;

import com.socialnetwork.socialnetwork.entity.Message;
import com.socialnetwork.socialnetwork.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessageByConversationId(@Param("conversationId") UUID conversationId);
    
    List<Message> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(UUID recipientId);
}
