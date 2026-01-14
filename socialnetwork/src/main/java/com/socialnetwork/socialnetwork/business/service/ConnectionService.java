package com.socialnetwork.socialnetwork.business.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IConnectionRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IConnectionService;
import com.socialnetwork.socialnetwork.business.interfaces.service.INotificationService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPrivacySettingsService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.Notification;
import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.ConnectionStatus;
import com.socialnetwork.socialnetwork.enums.NotificationType;

@Service
public class ConnectionService implements IConnectionService {

    private final IConnectionRepository repository;
    private final IUserService userService;
    private final IPrivacySettingsService privacySettingsService;
    private final INotificationService notificationService;

    public ConnectionService(IConnectionRepository repository,
                             IUserService userService,
                             IPrivacySettingsService privacySettingsService,
                             INotificationService notificationService) {
        this.repository = repository;
        this.userService = userService;
        this.privacySettingsService = privacySettingsService;
        this.notificationService = notificationService;
    }

    @Override
    public ResponseEntity<Connection> sendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Verify privacy setting of receiver
        ResponseEntity<PrivacySettings> psResp = privacySettingsService.getPrivacySettingsByUserID(receiverId);
        if (psResp.getStatusCode().is2xxSuccessful()) {
            PrivacySettings ps = psResp.getBody();
            if (ps != null && Boolean.FALSE.equals(ps.getAllowFriendRequests())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        // Check existing connections between users
        List<Connection> existing = repository.findAnyBetween(requesterId, receiverId);
        for (Connection c : existing) {
            if (c.getStatus() == ConnectionStatus.ACCEPTED || c.getStatus() == ConnectionStatus.PENDING) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        ResponseEntity<User> reqUserResp = userService.getUserById(requesterId);
        ResponseEntity<User> recUserResp = userService.getUserById(receiverId);
        if (!reqUserResp.getStatusCode().is2xxSuccessful() || !recUserResp.getStatusCode().is2xxSuccessful()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Connection connection = new Connection();
        connection.setRequester(reqUserResp.getBody());
        connection.setReceiver(recUserResp.getBody());
        connection.setStatus(ConnectionStatus.PENDING);

        Connection saved = repository.save(connection);

        // create notification for receiver
        Notification notif = new Notification();
        notif.setRecipient(recUserResp.getBody());
        notif.setActor(reqUserResp.getBody());
        notif.setNotificationType(NotificationType.FRIEND_REQUEST);
        notif.setContent("Nouvelle demande de connexion");
        notif.setIsRead(false);
        notif.setReadAt(null);
        notificationService.save(notif);

        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Connection> acceptRequest(UUID receiverId, UUID requesterId) {
        Optional<Connection> opt = repository.findByRequester_IdAndReceiver_Id(requesterId, receiverId);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Connection c = opt.get();
        if (c.getStatus() != ConnectionStatus.PENDING) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        c.setStatus(ConnectionStatus.ACCEPTED);
        Connection saved = repository.save(c);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> declineRequest(UUID receiverId, UUID requesterId) {
        Optional<Connection> opt = repository.findByRequester_IdAndReceiver_Id(requesterId, receiverId);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Connection c = opt.get();
        if (c.getStatus() != ConnectionStatus.PENDING) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        repository.delete(c);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Connection>> getPendingFor(UUID receiverId) {
        List<Connection> pending = repository.findByReceiver_IdAndStatus(receiverId, ConnectionStatus.PENDING);
        return new ResponseEntity<>(pending, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Connection>> getSentRequestsFor(UUID requesterId) {
        List<Connection> sent = repository.findByRequester_IdAndStatus(requesterId, ConnectionStatus.PENDING);
        return new ResponseEntity<List<Connection>>(sent, HttpStatus.OK);
    }
    
    @Override
    public List<Connection> findAllAcceptedRequestByUserID(UUID requesterId) {
        List<Connection> acceptedRequest = repository.findAllAcceptedRequestByUserID(requesterId);
        return acceptedRequest;
    }
}
