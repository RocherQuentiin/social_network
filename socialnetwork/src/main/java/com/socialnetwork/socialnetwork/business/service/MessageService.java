package com.socialnetwork.socialnetwork.business.service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IMessageRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IConversationRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IMessageService;
import com.socialnetwork.socialnetwork.entity.Message;
import com.socialnetwork.socialnetwork.entity.Conversation;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService implements IMessageService {
    
    private final IMessageRepository messageRepository;
    private final IConversationRepository conversationRepository;
    
    public MessageService(IMessageRepository messageRepository, IConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }
    
    @Override
    public ResponseEntity<Conversation> getOrCreateConversation(User user1, User user2) {
        Optional<Conversation> existing = conversationRepository.findConversationBetweenUsers(user1.getId(), user2.getId());
        
        if (existing.isPresent()) {
            return new ResponseEntity<>(existing.get(), HttpStatus.OK);
        }
        
        Conversation newConversation = new Conversation();
        newConversation.setParticipant1(user1);
        newConversation.setParticipant2(user2);
        
        Conversation saved = conversationRepository.save(newConversation);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    @Override
    public ResponseEntity<Message> sendMessage(Conversation conversation, User sender, User recipient, String content) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setIsRead(false);
        
        Message saved = messageRepository.save(message);
        
        // Update conversation last message and timestamp
        conversation.setLastMessage(saved);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    
    @Override
    public ResponseEntity<List<Message>> getConversationMessages(UUID conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<List<Conversation>> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<Message> markAsRead(UUID messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        
        if (message.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        message.get().setIsRead(true);
        message.get().setReadAt(LocalDateTime.now());
        Message saved = messageRepository.save(message.get());
        
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<List<Message>> getUnreadMessages(UUID recipientId) {
        List<Message> unreadMessages = messageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        return new ResponseEntity<>(unreadMessages, HttpStatus.OK);
    }
}
