package com.socialnetwork.socialnetwork.business.interfaces.service;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Notification;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    ResponseEntity<Notification> save(Notification notification);
    
    Notification createNotification(User recipient, User actor, NotificationType type, String content);
    ResponseEntity<List<Notification>> getUserNotifications(UUID userId);
    ResponseEntity<List<Notification>> getUnreadNotifications(UUID userId);
    ResponseEntity<Notification> markAsRead(UUID notificationId);
    ResponseEntity<Void> markAllAsRead(UUID userId);
    long getUnreadCount(UUID userId);
}
