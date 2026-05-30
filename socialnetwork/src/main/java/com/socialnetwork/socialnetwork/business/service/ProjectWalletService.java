package com.socialnetwork.socialnetwork.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectPaymentRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectWalletService;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectPayment;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;

@Service
public class ProjectWalletService implements IProjectWalletService {

    private static final int SCALE = 2;

    private final IUserRepository userRepository;
    private final IProjectPaymentRepository projectPaymentRepository;

    public ProjectWalletService(IUserRepository userRepository,
                                IProjectPaymentRepository projectPaymentRepository) {
        this.userRepository = userRepository;
        this.projectPaymentRepository = projectPaymentRepository;
    }

    @Override
    @Transactional
    public void recordCreatorIncomeOnSuccessfulPayment(UUID creatorId, BigDecimal amount) {
        if (creatorId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        User creator = userRepository.findById(creatorId).orElse(null);
        if (creator == null) {
            return;
        }
        BigDecimal next = creator.getWalletBalance().add(amount).setScale(SCALE, RoundingMode.HALF_UP);
        userRepository.updateWalletBalance(creatorId, next);
    }

    @Override
    @Transactional
    public boolean transferWalletFromPayerToCreator(UUID payerId, UUID creatorId, BigDecimal amount) {
        if (payerId == null || creatorId == null || payerId.equals(creatorId)) {
            return false;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        User payer = userRepository.findById(payerId).orElse(null);
        User creator = userRepository.findById(creatorId).orElse(null);
        if (payer == null || creator == null) {
            return false;
        }
        BigDecimal payerBal = payer.getWalletBalance();
        if (payerBal.compareTo(amount) < 0) {
            return false;
        }
        BigDecimal payerNext = payerBal.subtract(amount).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal creatorNext = creator.getWalletBalance().add(amount).setScale(SCALE, RoundingMode.HALF_UP);
        userRepository.updateWalletBalance(payerId, payerNext);
        userRepository.updateWalletBalance(creatorId, creatorNext);
        return true;
    }

    @Override
    @Transactional
    public void refundAllSuccessfulPaymentsForProject(Project project) {
        List<ProjectPayment> payments = projectPaymentRepository.findByProjectAndStatusAndRefunded(
                project, PaymentStatus.SUCCESS, false);
        for (ProjectPayment payment : payments) {
            refundSinglePayment(payment);
        }
    }

    @Override
    @Transactional
    public void refundSuccessfulPaymentsForLeavingMember(Project project, User leaver) {
        if (leaver == null) {
            return;
        }
        List<ProjectPayment> payments = projectPaymentRepository.findByProjectAndStatusAndPayerAndRefunded(
                project, PaymentStatus.SUCCESS, leaver, false);
        for (ProjectPayment payment : payments) {
            refundSinglePayment(payment);
        }
    }

    private void refundSinglePayment(ProjectPayment payment) {
        if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
            return;
        }
        if (Boolean.TRUE.equals(payment.getRefunded())) {
            return;
        }
        Project project = payment.getProject();
        User payer = payment.getPayer();
        if (project == null || payer == null || project.getCreator() == null) {
            return;
        }
        UUID creatorId = project.getCreator().getId();
        User creator = userRepository.findById(creatorId).orElse(null);
        User payerFresh = userRepository.findById(payer.getId()).orElse(null);
        if (creator == null || payerFresh == null) {
            return;
        }
        BigDecimal amt = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        if (amt.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal payerNext = payerFresh.getWalletBalance().add(amt).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal creatorNext = creator.getWalletBalance().subtract(amt).setScale(SCALE, RoundingMode.HALF_UP);
        if (creatorNext.compareTo(BigDecimal.ZERO) < 0) {
            creatorNext = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }

        payment.setRefunded(true);
        payment.setRefundedAt(LocalDateTime.now());

        userRepository.updateWalletBalance(payerFresh.getId(), payerNext);
        userRepository.updateWalletBalance(creator.getId(), creatorNext);
        projectPaymentRepository.save(payment);
    }
}
