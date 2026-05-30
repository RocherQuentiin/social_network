package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.EventPayment;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;

public interface IEventPaymentRepository extends JpaRepository<EventPayment, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(ep) > 0 THEN true ELSE false END
            FROM EventPayment ep
            WHERE ep.event = :event AND ep.payer = :payer AND ep.status = :status
              AND (ep.refunded = false OR ep.refunded IS NULL)
            """)
    boolean existsSuccessfulPaymentNotRefunded(@Param("event") Event event,
                                               @Param("payer") User payer,
                                               @Param("status") PaymentStatus status);

    List<EventPayment> findByEventAndStatusAndRefunded(Event event, PaymentStatus status, boolean refunded);

    List<EventPayment> findByEventAndStatusAndPayerAndRefunded(Event event, PaymentStatus status, User payer, boolean refunded);
}
