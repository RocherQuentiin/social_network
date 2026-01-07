package com.socialnetwork.socialnetwork.business.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.INotificationRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.INotificationService;
import com.socialnetwork.socialnetwork.entity.Notification;

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
}
