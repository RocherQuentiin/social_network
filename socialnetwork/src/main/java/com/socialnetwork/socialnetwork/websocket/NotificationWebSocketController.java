package com.socialnetwork.socialnetwork.websocket;

import com.socialnetwork.socialnetwork.dto.NotificationDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

    @MessageMapping("/notify/{userId}")
    @SendTo("/topic/user/{userId}")
    public NotificationDTO handleNotification(
            @DestinationVariable String userId, 
            NotificationDTO notification) {
        try {
            notification.setTimestamp(System.currentTimeMillis());
            System.out.println("Notification sent to user: " + userId);
            return notification;
        } catch (Exception e) {
            System.err.println("Error in handleNotification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
