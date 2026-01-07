package com.socialnetwork.socialnetwork.business.interfaces.service;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Notification;

public interface INotificationService {
    ResponseEntity<Notification> save(Notification notification);
}
