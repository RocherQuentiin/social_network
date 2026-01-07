package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Notification;

public interface INotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipient_IdAndIsReadFalse(UUID recipientId);
}
