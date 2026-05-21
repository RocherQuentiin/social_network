package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.dto.ProjectPaymentRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentResponseDto;

public interface IProjectPaymentService {

    ResponseEntity<ProjectPaymentResponseDto> processProjectPayment(UUID projectId, UUID userId, ProjectPaymentRequestDto paymentRequestDto);

    BigDecimal getTotalRevenueForCreator(UUID creatorId);

    /** Paiement SUCCESS non remboursé (sortie + remboursement exclut l'ancienne ligne). */
    boolean hasSuccessfulPayment(UUID projectId, UUID userId);

    /** Réadhésion : uniquement si un paiement SUCCESS non remboursé existe encore. */
    void ensureMembershipAfterSuccessfulPayment(UUID projectId, UUID userId);
}
