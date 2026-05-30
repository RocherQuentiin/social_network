package com.socialnetwork.socialnetwork.business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventAttendeeRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventPaymentRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IEventRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventPaymentService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectWalletService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserPaymentMethodService;
import com.socialnetwork.socialnetwork.business.utils.PaymentTestCardUtils;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentResponseDto;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodRequestDto;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.EventAttendee;
import com.socialnetwork.socialnetwork.entity.EventPayment;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.entity.UserPaymentMethod;
import com.socialnetwork.socialnetwork.enums.EventAttendanceStatus;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;

@Service
public class EventPaymentService implements IEventPaymentService {

    private final IEventRepository eventRepository;
    private final IUserRepository userRepository;
    private final IEventAttendeeRepository eventAttendeeRepository;
    private final IEventPaymentRepository eventPaymentRepository;
    private final IProjectWalletService projectWalletService;
    private final IUserPaymentMethodService userPaymentMethodService;

    public EventPaymentService(IEventRepository eventRepository,
                               IUserRepository userRepository,
                               IEventAttendeeRepository eventAttendeeRepository,
                               IEventPaymentRepository eventPaymentRepository,
                               IProjectWalletService projectWalletService,
                               IUserPaymentMethodService userPaymentMethodService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventAttendeeRepository = eventAttendeeRepository;
        this.eventPaymentRepository = eventPaymentRepository;
        this.projectWalletService = projectWalletService;
        this.userPaymentMethodService = userPaymentMethodService;
    }

    @Override
    @Transactional
    public ResponseEntity<ProjectPaymentResponseDto> processEventPayment(UUID eventId, UUID userId,
                                                                         ProjectPaymentRequestDto dto) {
        ProjectPaymentRequestDto paymentRequestDto = dto != null ? dto : new ProjectPaymentRequestDto();

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ProjectPaymentResponseDto(false, "Événement ou utilisateur introuvable", null));
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (!Boolean.TRUE.equals(event.getIsPaid())) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Cet événement n'est pas payant", null));
        }

        if (event.getCreator() != null && event.getCreator().getId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Vous ne pouvez pas payer votre propre événement", null));
        }

        if (isAlreadyAcceptedAttendee(event, user)) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Vous participez déjà à cet événement",
                            buildSuccessRedirect(eventId, paymentRequestDto.getReturnTo())));
        }

        if (eventPaymentRepository.existsSuccessfulPaymentNotRefunded(event, user, PaymentStatus.SUCCESS)) {
            addAttendeeIfMissing(event, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true, "Paiement déjà effectué",
                    buildSuccessRedirect(eventId, paymentRequestDto.getReturnTo())));
        }

        if (!hasAvailableCapacity(event)) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "L'événement est complet.", null));
        }

        BigDecimal price = event.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Prix de l'événement invalide", null));
        }

        UUID creatorId = event.getCreator() != null ? event.getCreator().getId() : null;
        if (creatorId == null) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Créateur de l'événement introuvable", null));
        }

        if (Boolean.TRUE.equals(paymentRequestDto.getUseWalletBalance())) {
            if (paymentRequestDto.getSavedPaymentMethodId() != null) {
                return ResponseEntity.badRequest()
                        .body(new ProjectPaymentResponseDto(false, "Choisissez soit le solde, soit une carte.", null));
            }
            if (!projectWalletService.transferWalletFromPayerToCreator(userId, creatorId, price)) {
                return ResponseEntity.badRequest()
                        .body(new ProjectPaymentResponseDto(false,
                                "Solde insuffisant dans votre portefeuille projet.", null));
            }
            EventPayment payment = buildWalletPayment(event, user, price);
            eventPaymentRepository.save(payment);
            addAttendeeIfMissing(event, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                    "Paiement par solde réussi — vous participez maintenant à l'événement.",
                    buildSuccessRedirect(eventId, paymentRequestDto.getReturnTo())));
        }

        if (paymentRequestDto.getSavedPaymentMethodId() != null) {
            Optional<UserPaymentMethod> methodOpt = userPaymentMethodService.findOwned(userId,
                    paymentRequestDto.getSavedPaymentMethodId());
            if (methodOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ProjectPaymentResponseDto(false, "Moyen de paiement introuvable.", null));
            }
            UserPaymentMethod method = methodOpt.get();
            if (!PaymentTestCardUtils.validateSavedCard(method.getCardLast4(), method.getExpiryMonth(),
                    method.getExpiryYear(), paymentRequestDto.getCvv())) {
                return ResponseEntity.badRequest()
                        .body(new ProjectPaymentResponseDto(false,
                                "CVV ou date d'expiration incorrecte pour cette carte.", null));
            }
            EventPayment payment = buildPaymentFromSaved(event, user, method);
            eventPaymentRepository.save(payment);
            projectWalletService.recordCreatorIncomeOnSuccessfulPayment(creatorId, payment.getAmount());
            addAttendeeIfMissing(event, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                    "Paiement réussi — vous participez maintenant à l'événement.",
                    buildSuccessRedirect(eventId, paymentRequestDto.getReturnTo())));
        }

        boolean valid = isValidTestPayment(paymentRequestDto);
        EventPayment payment = buildPayment(event, user, paymentRequestDto, valid);
        eventPaymentRepository.save(payment);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ProjectPaymentResponseDto(false, "Paiement refusé. Vérifiez les informations de test.", null));
        }

        projectWalletService.recordCreatorIncomeOnSuccessfulPayment(creatorId, payment.getAmount());
        maybeSavePaymentMethod(userId, paymentRequestDto);
        addAttendeeIfMissing(event, user);

        return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                "Paiement réussi — vous participez maintenant à l'événement.",
                buildSuccessRedirect(eventId, paymentRequestDto.getReturnTo())));
    }

    @Override
    public boolean hasSuccessfulPayment(UUID eventId, UUID userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }
        return eventPaymentRepository.existsSuccessfulPaymentNotRefunded(eventOpt.get(), userOpt.get(), PaymentStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void ensureAttendanceAfterSuccessfulPayment(UUID eventId, UUID userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (eventOpt.isEmpty() || userOpt.isEmpty()) {
            return;
        }
        Event event = eventOpt.get();
        User user = userOpt.get();
        if (!Boolean.TRUE.equals(event.getIsPaid())) {
            return;
        }
        if (event.getCreator() != null && event.getCreator().getId().equals(userId)) {
            return;
        }
        if (!eventPaymentRepository.existsSuccessfulPaymentNotRefunded(event, user, PaymentStatus.SUCCESS)) {
            return;
        }
        addAttendeeIfMissing(event, user);
    }

    private void maybeSavePaymentMethod(UUID userId, ProjectPaymentRequestDto paymentRequestDto) {
        if (!Boolean.TRUE.equals(paymentRequestDto.getSavePaymentMethod())) {
            return;
        }
        UserPaymentMethodRequestDto m = new UserPaymentMethodRequestDto();
        m.setCardholderName(paymentRequestDto.getCardholderName());
        m.setCardNumber(paymentRequestDto.getCardNumber());
        m.setExpiryMonth(paymentRequestDto.getExpiryMonth());
        m.setExpiryYear(paymentRequestDto.getExpiryYear());
        m.setCvv(paymentRequestDto.getCvv());
        m.setLabel(null);
        userPaymentMethodService.create(userId, m);
    }

    private EventPayment buildWalletPayment(Event event, User user, BigDecimal amount) {
        EventPayment payment = new EventPayment();
        payment.setEvent(event);
        payment.setPayer(user);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCardLast4("WLET");
        payment.setPaymentReference("WALLET-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        payment.setRefunded(false);
        return payment;
    }

    private EventPayment buildPaymentFromSaved(Event event, User user, UserPaymentMethod method) {
        EventPayment payment = new EventPayment();
        payment.setEvent(event);
        payment.setPayer(user);
        payment.setAmount(event.getPrice());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCardLast4(method.getCardLast4());
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        payment.setRefunded(false);
        return payment;
    }

    private EventPayment buildPayment(Event event, User user, ProjectPaymentRequestDto paymentRequestDto, boolean success) {
        EventPayment payment = new EventPayment();
        payment.setEvent(event);
        payment.setPayer(user);
        payment.setAmount(event.getPrice());
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setCardLast4(PaymentTestCardUtils.lastFourDigits(paymentRequestDto.getCardNumber()));
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        payment.setPaidAt(success ? LocalDateTime.now() : null);
        payment.setRefunded(false);
        return payment;
    }

    private void addAttendeeIfMissing(Event event, User user) {
        Optional<EventAttendee> existing = eventAttendeeRepository.findByEvent_idAndUser_id(event.getId(), user.getId());
        if (existing.isPresent()) {
            EventAttendee attendee = existing.get();
            if (attendee.getStatus() != EventAttendanceStatus.ACCEPTED) {
                attendee.setStatus(EventAttendanceStatus.ACCEPTED);
                eventAttendeeRepository.save(attendee);
            }
            return;
        }

        EventAttendee attendee = new EventAttendee();
        attendee.setEvent(event);
        attendee.setUser(user);
        attendee.setStatus(EventAttendanceStatus.ACCEPTED);
        eventAttendeeRepository.save(attendee);
    }

    private boolean isAlreadyAcceptedAttendee(Event event, User user) {
        return eventAttendeeRepository.findByEvent_idAndUser_id(event.getId(), user.getId())
                .map(a -> a.getStatus() == EventAttendanceStatus.ACCEPTED)
                .orElse(false);
    }

    private boolean hasAvailableCapacity(Event event) {
        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            return true;
        }
        List<EventAttendee> accepted = eventAttendeeRepository.findByEvent_idAndStatus(
                event.getId(), EventAttendanceStatus.ACCEPTED);
        return accepted.size() < event.getCapacity();
    }

    private boolean isValidTestPayment(ProjectPaymentRequestDto paymentRequestDto) {
        if (paymentRequestDto == null) {
            return false;
        }
        return PaymentTestCardUtils.validateFullCard(
                paymentRequestDto.getCardholderName(),
                paymentRequestDto.getCardNumber(),
                paymentRequestDto.getExpiryMonth(),
                paymentRequestDto.getExpiryYear(),
                paymentRequestDto.getCvv());
    }

    private String buildSuccessRedirect(UUID eventId, String returnTo) {
        String base = sanitizeReturnTo(returnTo);
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "payment=success&eventId=" + eventId;
    }

    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) {
            return "/feed";
        }
        if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/feed";
        }
        return returnTo;
    }
}
