package com.socialnetwork.socialnetwork.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventPaymentService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/event")
public class EventPaymentController {

    private final IEventPaymentService eventPaymentService;

    public EventPaymentController(IEventPaymentService eventPaymentService) {
        this.eventPaymentService = eventPaymentService;
    }

    @PostMapping("/{eventId}/payment")
    public ResponseEntity<ProjectPaymentResponseDto> processEventPayment(@PathVariable UUID eventId,
                                                                           @RequestBody ProjectPaymentRequestDto paymentRequestDto,
                                                                           HttpServletRequest request) {
        Object userIsConnected = Utils.validPage(request, true);
        if (userIsConnected == null) {
            return ResponseEntity.status(403)
                    .body(new ProjectPaymentResponseDto(false, "Authentication required", null));
        }

        UUID userId = UUID.fromString(userIsConnected.toString());
        return eventPaymentService.processEventPayment(eventId, userId, paymentRequestDto);
    }
}
