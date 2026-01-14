package com.socialnetwork.socialnetwork.websocket;

import com.socialnetwork.socialnetwork.dto.MessageDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageWebSocketController {

    @MessageMapping("/send-message/{conversationId}")
    @SendTo("/topic/conversation/{conversationId}")
    public MessageDTO handleMessage(
            @DestinationVariable String conversationId, 
            MessageDTO message) {
        try {
            message.setTimestamp(System.currentTimeMillis());
            System.out.println("Message received for conversation: " + conversationId);
            return message;
        } catch (Exception e) {
            System.err.println("Error in handleMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
