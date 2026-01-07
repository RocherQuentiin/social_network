package com.socialnetwork.socialnetwork.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IConnectionService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Connection;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/friend-request")
public class FriendRequestController {

    private final IConnectionService connectionService;

    public FriendRequestController(IConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendFriendRequest(HttpServletRequest request, @RequestParam("userId") String userId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID requesterId = UUID.fromString(userIsConnect.toString());
        UUID receiverId = UUID.fromString(userId);

        ResponseEntity<Connection> resp = connectionService.sendRequest(requesterId, receiverId);
        if (resp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.OK).body("Friend request sent");
        } else if (resp.getStatusCode() == HttpStatusCode.valueOf(409)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Request already exists or already friends");
        } else if (resp.getStatusCode() == HttpStatusCode.valueOf(403)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not accept friend requests");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to send friend request");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendRequest(HttpServletRequest request,
                                                       @RequestParam("requesterId") String requesterId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID receiverId = UUID.fromString(userIsConnect.toString());
        UUID reqId = UUID.fromString(requesterId);

        ResponseEntity<Connection> resp = connectionService.acceptRequest(receiverId, reqId);
        if (resp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.OK).body("Friend request accepted");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to accept friend request");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineFriendRequest(HttpServletRequest request,
                                                        @RequestParam("requesterId") String requesterId) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }

        UUID receiverId = UUID.fromString(userIsConnect.toString());
        UUID reqId = UUID.fromString(requesterId);

        ResponseEntity<Void> resp = connectionService.declineRequest(receiverId, reqId);
        if (resp.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.OK).body("Friend request declined");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to decline friend request");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Connection>> getPendingRequests(HttpServletRequest request) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID receiverId = UUID.fromString(userIsConnect.toString());
        return connectionService.getPendingFor(receiverId);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Connection>> getSentRequests(HttpServletRequest request) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID requesterId = UUID.fromString(userIsConnect.toString());
        return connectionService.getSentRequestsFor(requesterId);
    }
}
