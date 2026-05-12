package com.socialnetwork.socialnetwork.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserPaymentMethodService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodRequestDto;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/payment-methods")
public class UserPaymentMethodController {

    private final IUserPaymentMethodService userPaymentMethodService;

    public UserPaymentMethodController(IUserPaymentMethodService userPaymentMethodService) {
        this.userPaymentMethodService = userPaymentMethodService;
    }

    @GetMapping
    public ResponseEntity<List<UserPaymentMethodResponseDto>> list(HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = UUID.fromString(userIsConnected.toString());
        return ResponseEntity.ok(userPaymentMethodService.listForUser(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserPaymentMethodRequestDto dto, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentification requise"));
        }
        UUID userId = UUID.fromString(userIsConnected.toString());
        Optional<UserPaymentMethodResponseDto> created = userPaymentMethodService.create(userId, dto);
        if (created.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Carte invalide (démo : 4242…4242, CVV 123, date future)."));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = UUID.fromString(userIsConnected.toString());
        if (!userPaymentMethodService.delete(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
