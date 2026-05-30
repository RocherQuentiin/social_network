package com.socialnetwork.socialnetwork.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventPaymentRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventWalletService;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.EventPayment;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;

@Service
public class EventWalletService implements IEventWalletService {

    private static final int SCALE = 2;

    private final IUserRepository userRepository;
    private final IEventPaymentRepository eventPaymentRepository;

    public EventWalletService(IUserRepository userRepository,
                              IEventPaymentRepository eventPaymentRepository) {
        this.userRepository = userRepository;
        this.eventPaymentRepository = eventPaymentRepository;
    }

    @Override
    @Transactional
    public void refundAllSuccessfulPaymentsForEvent(Event event) {
        List<EventPayment> payments = eventPaymentRepository.findByEventAndStatusAndRefunded(
                event, PaymentStatus.SUCCESS, false);
        for (EventPayment payment : payments) {
            refundSinglePayment(payment);
        }
    }

    @Override
    @Transactional
    public void refundSuccessfulPaymentsForLeavingAttendee(Event event, User leaver) {
        if (leaver == null) {
            return;
        }
        List<EventPayment> payments = eventPaymentRepository.findByEventAndStatusAndPayerAndRefunded(
                event, PaymentStatus.SUCCESS, leaver, false);
        for (EventPayment payment : payments) {
            refundSinglePayment(payment);
        }
    }

    private void refundSinglePayment(EventPayment payment) {
        if (payment == null || payment.getStatus() != PaymentStatus.SUCCESS) {
            return;
        }
        if (Boolean.TRUE.equals(payment.getRefunded())) {
            return;
        }
        Event event = payment.getEvent();
        User payer = payment.getPayer();
        if (event == null || payer == null || event.getCreator() == null) {
            return;
        }
        User creator = userRepository.findById(event.getCreator().getId()).orElse(null);
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
        eventPaymentRepository.save(payment);
    }
}
