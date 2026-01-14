package com.socialnetwork.socialnetwork.websocket;

import com.socialnetwork.socialnetwork.dto.ProjectMessageDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ProjectMessageWebSocketController {

    @MessageMapping("/send-project-message/{messageGroupId}")
    @SendTo("/topic/project-group/{messageGroupId}")
    public ProjectMessageDTO handleProjectMessage(
            @DestinationVariable String messageGroupId, 
            ProjectMessageDTO message) {
        try {
            message.setTimestamp(System.currentTimeMillis());
            System.out.println("Project message received for group: " + messageGroupId);
            return message;
        } catch (Exception e) {
            System.err.println("Error in handleProjectMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
