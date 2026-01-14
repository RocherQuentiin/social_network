package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.IMessageService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.dto.MessageDTO;
import com.socialnetwork.socialnetwork.entity.Message;
import com.socialnetwork.socialnetwork.entity.Conversation;
import com.socialnetwork.socialnetwork.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    private final IMessageService messageService;
    private final IUserService userService;
    
    public MessageController(IMessageService messageService, IUserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }
    
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<?> getConversation(HttpServletRequest request, @PathVariable UUID otherUserId) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        ResponseEntity<User> currentUser = userService.getUserById(userId);
        ResponseEntity<User> otherUser = userService.getUserById(otherUserId);
        
        if (currentUser.getStatusCode() != HttpStatusCode.valueOf(200) || 
            otherUser.getStatusCode() != HttpStatusCode.valueOf(200)) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        return messageService.getOrCreateConversation(currentUser.getBody(), otherUser.getBody());
    }
    
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable UUID conversationId) {
        ResponseEntity<List<Message>> messages = messageService.getConversationMessages(conversationId);
        
        List<MessageDTO> dtos = messages.getBody().stream()
            .map(msg -> new MessageDTO(
                msg.getId(),
                msg.getConversation().getId(),
                msg.getSender().getId(),
                msg.getSender().getUsername(),
                msg.getContent()
            ))
            .collect(Collectors.toList());
        
        return new ResponseEntity<>(dtos, messages.getStatusCode());
    }
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            HttpServletRequest request,
            @RequestParam UUID conversationId,
            @RequestParam UUID recipientId,
            @RequestParam String content) {
        
        try {
            System.out.println("=== Message Send Request ===");
            System.out.println("ConversationId: " + conversationId);
            System.out.println("RecipientId: " + recipientId);
            System.out.println("Content: " + content);
            
            HttpSession session = request.getSession();
            Object userIdObj = session.getAttribute("userId");
            
            if (userIdObj == null) {
                System.err.println("ERROR: No userId in session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new java.util.HashMap<String, String>() {{ put("error", "Unauthorized"); }});
            }
            
            UUID senderId = UUID.fromString(userIdObj.toString());
            System.out.println("SenderId: " + senderId);
            
            ResponseEntity<User> sender = userService.getUserById(senderId);
            ResponseEntity<User> recipient = userService.getUserById(recipientId);
            
            System.out.println("Sender status: " + sender.getStatusCode());
            System.out.println("Recipient status: " + recipient.getStatusCode());
            
            if (!sender.getStatusCode().is2xxSuccessful() || !recipient.getStatusCode().is2xxSuccessful()) {
                System.err.println("ERROR: User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new java.util.HashMap<String, String>() {{ put("error", "User not found"); }});
            }
            
            ResponseEntity<Conversation> conversation = messageService.getOrCreateConversation(
                sender.getBody(), recipient.getBody());
            
            System.out.println("Conversation status: " + conversation.getStatusCode());
            
            ResponseEntity<Message> message = messageService.sendMessage(
                conversation.getBody(),
                sender.getBody(),
                recipient.getBody(),
                content
            );
            
            Message msg = message.getBody();
            if (msg == null) {
                System.err.println("ERROR: Message creation returned null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, String>() {{ put("error", "Failed to create message"); }});
            }
            
            MessageDTO dto = new MessageDTO(
                msg.getId(),
                msg.getConversation().getId(),
                msg.getSender().getId(),
                msg.getSender().getUsername(),
                msg.getContent()
            );
            dto.setSenderAvatar(msg.getSender().getProfilePictureUrl());
            dto.setTimestamp(msg.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            dto.setIsRead(msg.getIsRead());
            
            System.out.println("Message sent successfully: " + msg.getId());
            return new ResponseEntity<>(dto, message.getStatusCode());
        } catch (Exception e) {
            System.err.println("ERROR in sendMessage: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new java.util.HashMap<String, String>() {{ put("error", e.getMessage()); }});
        }
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<List<?>> getConversations(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        ResponseEntity<List<Conversation>> response = messageService.getUserConversations(userId);
        
        List<?> dtos = response.getBody().stream()
            .map(conv -> {
                User otherUser = conv.getParticipant1().getId().equals(userId) ? 
                    conv.getParticipant2() : conv.getParticipant1();
                
                return new java.util.LinkedHashMap<String, Object>() {{
                    put("conversationId", conv.getId());
                    put("otherUserId", otherUser.getId());
                    put("otherUserName", otherUser.getUsername());
                    put("otherUserAvatar", otherUser.getProfilePictureUrl());
                    put("lastMessage", conv.getLastMessage() != null ? conv.getLastMessage().getContent() : "");
                    put("updatedAt", conv.getUpdatedAt());
                }};
            })
            .collect(Collectors.toList());
        
        return new ResponseEntity<>(dtos, response.getStatusCode());
    }
    
    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID messageId) {
        messageService.markAsRead(messageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        ResponseEntity<List<Message>> unreadMessages = messageService.getUnreadMessages(userId);
        long count = unreadMessages.getBody().size();
        
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
