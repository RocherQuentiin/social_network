package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.INotificationService;
import com.socialnetwork.socialnetwork.entity.Notification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final INotificationService notificationService;
    
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        return notificationService.getUserNotifications(userId);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        return notificationService.getUnreadNotifications(userId);
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        long count = notificationService.getUnreadCount(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
    
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID notificationId) {
        return notificationService.markAsRead(notificationId);
    }
    
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userId = UUID.fromString(session.getAttribute("userId").toString());
        
        return notificationService.markAllAsRead(userId);
    }
}
