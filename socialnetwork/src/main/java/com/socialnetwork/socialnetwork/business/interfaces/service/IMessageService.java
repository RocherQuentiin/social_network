package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.entity.Message;
import com.socialnetwork.socialnetwork.entity.Conversation;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface IMessageService {
    ResponseEntity<Conversation> getOrCreateConversation(User user1, User user2);
    ResponseEntity<Message> sendMessage(Conversation conversation, User sender, User recipient, String content);
    ResponseEntity<List<Message>> getConversationMessages(UUID conversationId);
    ResponseEntity<List<Conversation>> getUserConversations(UUID userId);
    ResponseEntity<Message> markAsRead(UUID messageId);
    ResponseEntity<List<Message>> getUnreadMessages(UUID recipientId);
}
