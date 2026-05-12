package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;

public interface IProjectWalletService {

    void recordCreatorIncomeOnSuccessfulPayment(UUID creatorId, BigDecimal amount);

    /**
     * Transfert interne : débite le payeur et crédite le créateur (paiement projet depuis le solde).
     * @return false si solde payeur insuffisant ou utilisateurs introuvables
     */
    boolean transferWalletFromPayerToCreator(UUID payerId, UUID creatorId, BigDecimal amount);

    void refundAllSuccessfulPaymentsForProject(Project project);

    void refundSuccessfulPaymentsForLeavingMember(Project project, User leaver);
}
