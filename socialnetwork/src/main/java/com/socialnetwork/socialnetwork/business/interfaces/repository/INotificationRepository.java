package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Notification;

public interface INotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipient_IdAndIsReadFalse(UUID recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") UUID recipientId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false")
    long countUnreadNotifications(@Param("recipientId") UUID recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByRecipientId(@Param("recipientId") UUID recipientId);
}
