package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.dto.ProjectPaymentRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentResponseDto;

public interface IEventPaymentService {

    ResponseEntity<ProjectPaymentResponseDto> processEventPayment(UUID eventId, UUID userId,
                                                                  ProjectPaymentRequestDto dto);

    boolean hasSuccessfulPayment(UUID eventId, UUID userId);

    void ensureAttendanceAfterSuccessfulPayment(UUID eventId, UUID userId);
}
