package com.socialnetwork.socialnetwork.business.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.INotificationRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.INotificationService;
import com.socialnetwork.socialnetwork.entity.Notification;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService implements INotificationService {

    private final INotificationRepository repository;

    public NotificationService(INotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<Notification> save(Notification notification) {
        Notification saved = repository.save(notification);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @Override
    public Notification createNotification(User recipient, User actor, NotificationType type, String content) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setNotificationType(type);
        notification.setContent(content);
        notification.setIsRead(false);
        
        return repository.save(notification);
    }
    
    @Override
    public ResponseEntity<List<Notification>> getUserNotifications(UUID userId) {
        List<Notification> notifications = repository.findByRecipientIdOrderByCreatedAtDesc(userId);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<List<Notification>> getUnreadNotifications(UUID userId) {
        List<Notification> notifications = repository.findUnreadNotificationsByRecipientId(userId);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<Notification> markAsRead(UUID notificationId) {
        Optional<Notification> notification = repository.findById(notificationId);
        
        if (notification.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        notification.get().setIsRead(true);
        notification.get().setReadAt(LocalDateTime.now());
        Notification saved = repository.save(notification.get());
        
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<Void> markAllAsRead(UUID userId) {
        List<Notification> unread = repository.findUnreadNotificationsByRecipientId(userId);
        LocalDateTime now = LocalDateTime.now();
        
        for (Notification notification : unread) {
            notification.setIsRead(true);
            notification.setReadAt(now);
        }
        
        repository.saveAll(unread);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @Override
    public long getUnreadCount(UUID userId) {
        return repository.countUnreadNotifications(userId);
    }
}
