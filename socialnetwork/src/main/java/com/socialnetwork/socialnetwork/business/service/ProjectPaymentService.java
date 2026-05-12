package com.socialnetwork.socialnetwork.business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectMemberRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectPaymentRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IProjectRequestRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectPaymentService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectWalletService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserPaymentMethodService;
import com.socialnetwork.socialnetwork.business.utils.PaymentTestCardUtils;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentRequestDto;
import com.socialnetwork.socialnetwork.dto.ProjectPaymentResponseDto;
import com.socialnetwork.socialnetwork.dto.UserPaymentMethodRequestDto;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectMember;
import com.socialnetwork.socialnetwork.entity.ProjectPayment;
import com.socialnetwork.socialnetwork.entity.ProjectRequest;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.entity.UserPaymentMethod;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;
import com.socialnetwork.socialnetwork.enums.ProjectMemberRole;
import com.socialnetwork.socialnetwork.enums.ProjectRequestStatus;

@Service
public class ProjectPaymentService implements IProjectPaymentService {

    private final IProjectRepository projectRepository;
    private final IUserRepository userRepository;
    private final IProjectMemberRepository projectMemberRepository;
    private final IProjectPaymentRepository projectPaymentRepository;
    private final IProjectRequestRepository projectRequestRepository;
    private final IProjectWalletService projectWalletService;
    private final IUserPaymentMethodService userPaymentMethodService;

    public ProjectPaymentService(IProjectRepository projectRepository,
                                 IUserRepository userRepository,
                                 IProjectMemberRepository projectMemberRepository,
                                 IProjectPaymentRepository projectPaymentRepository,
                                 IProjectRequestRepository projectRequestRepository,
                                 IProjectWalletService projectWalletService,
                                 IUserPaymentMethodService userPaymentMethodService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPaymentRepository = projectPaymentRepository;
        this.projectRequestRepository = projectRequestRepository;
        this.projectWalletService = projectWalletService;
        this.userPaymentMethodService = userPaymentMethodService;
    }

    @Override
    @Transactional
    public ResponseEntity<ProjectPaymentResponseDto> processProjectPayment(UUID projectId, UUID userId,
                                                                           ProjectPaymentRequestDto dto) {
        ProjectPaymentRequestDto paymentRequestDto = dto != null ? dto : new ProjectPaymentRequestDto();

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (projectOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ProjectPaymentResponseDto(false, "Projet ou utilisateur introuvable", null));
        }

        Project project = projectOpt.get();
        User user = userOpt.get();

        if (!Boolean.TRUE.equals(project.getIsPaid())) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Ce projet n'est pas payant", null));
        }

        if (project.getCreator() != null && project.getCreator().getId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Vous ne pouvez pas payer votre propre projet", null));
        }

        if (projectMemberRepository.findByProjectAndUser(project, user).isPresent()) {
            cancelPendingJoinRequest(project, user);
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Vous êtes déjà membre de ce projet",
                            buildSuccessRedirect(projectId, paymentRequestDto.getReturnTo())));
        }

        if (projectPaymentRepository.existsSuccessfulPaymentNotRefunded(project, user, PaymentStatus.SUCCESS)) {
            addMemberIfMissing(project, user);
            cancelPendingJoinRequest(project, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true, "Paiement déjà effectué",
                    buildSuccessRedirect(projectId, paymentRequestDto.getReturnTo())));
        }

        BigDecimal price = project.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Prix du projet invalide", null));
        }

        UUID creatorId = project.getCreator() != null ? project.getCreator().getId() : null;
        if (creatorId == null) {
            return ResponseEntity.badRequest()
                    .body(new ProjectPaymentResponseDto(false, "Créateur du projet introuvable", null));
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
            ProjectPayment payment = buildWalletPayment(project, user, price);
            projectPaymentRepository.save(payment);
            addMemberIfMissing(project, user);
            cancelPendingJoinRequest(project, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                    "Paiement par solde réussi — vous êtes maintenant membre du projet.",
                    buildSuccessRedirect(projectId, paymentRequestDto.getReturnTo())));
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
            ProjectPayment payment = buildPaymentFromSaved(project, user, method);
            projectPaymentRepository.save(payment);
            projectWalletService.recordCreatorIncomeOnSuccessfulPayment(creatorId, payment.getAmount());
            addMemberIfMissing(project, user);
            cancelPendingJoinRequest(project, user);
            return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                    "Paiement réussi — vous êtes maintenant membre du projet.",
                    buildSuccessRedirect(projectId, paymentRequestDto.getReturnTo())));
        }

        boolean valid = isValidTestPayment(paymentRequestDto);
        ProjectPayment payment = buildPayment(project, user, paymentRequestDto, valid);
        projectPaymentRepository.save(payment);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ProjectPaymentResponseDto(false, "Paiement refusé. Vérifiez les informations de test.", null));
        }

        projectWalletService.recordCreatorIncomeOnSuccessfulPayment(creatorId, payment.getAmount());
        maybeSavePaymentMethod(userId, paymentRequestDto);
        addMemberIfMissing(project, user);
        cancelPendingJoinRequest(project, user);

        return ResponseEntity.ok(new ProjectPaymentResponseDto(true,
                "Paiement réussi — vous êtes maintenant membre du projet.",
                buildSuccessRedirect(projectId, paymentRequestDto.getReturnTo())));
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

    private ProjectPayment buildWalletPayment(Project project, User user, BigDecimal amount) {
        ProjectPayment payment = new ProjectPayment();
        payment.setProject(project);
        payment.setPayer(user);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCardLast4("WLET");
        payment.setPaymentReference("WALLET-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        payment.setRefunded(false);
        return payment;
    }

    private ProjectPayment buildPaymentFromSaved(Project project, User user, UserPaymentMethod method) {
        ProjectPayment payment = new ProjectPayment();
        payment.setProject(project);
        payment.setPayer(user);
        payment.setAmount(project.getPrice());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCardLast4(method.getCardLast4());
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        payment.setPaidAt(LocalDateTime.now());
        payment.setRefunded(false);
        return payment;
    }

    @Override
    public BigDecimal getTotalRevenueForCreator(UUID creatorId) {
        BigDecimal total = projectPaymentRepository.sumRevenueByCreatorAndStatus(creatorId, PaymentStatus.SUCCESS);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public boolean hasSuccessfulPayment(UUID projectId, UUID userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (projectOpt.isEmpty() || userOpt.isEmpty()) {
            return false;
        }
        return projectPaymentRepository.existsSuccessfulPaymentNotRefunded(projectOpt.get(), userOpt.get(), PaymentStatus.SUCCESS);
    }

    @Override
    @Transactional
    public void ensureMembershipAfterSuccessfulPayment(UUID projectId, UUID userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (projectOpt.isEmpty() || userOpt.isEmpty()) {
            return;
        }
        Project project = projectOpt.get();
        User user = userOpt.get();
        if (!Boolean.TRUE.equals(project.getIsPaid())) {
            return;
        }
        if (project.getCreator() != null && project.getCreator().getId().equals(userId)) {
            return;
        }
        if (!projectPaymentRepository.existsSuccessfulPaymentNotRefunded(project, user, PaymentStatus.SUCCESS)) {
            return;
        }
        addMemberIfMissing(project, user);
        cancelPendingJoinRequest(project, user);
    }

    private ProjectPayment buildPayment(Project project, User user, ProjectPaymentRequestDto paymentRequestDto, boolean success) {
        ProjectPayment payment = new ProjectPayment();
        payment.setProject(project);
        payment.setPayer(user);
        payment.setAmount(project.getPrice());
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setCardLast4(PaymentTestCardUtils.lastFourDigits(paymentRequestDto.getCardNumber()));
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        payment.setPaidAt(success ? LocalDateTime.now() : null);
        payment.setRefunded(false);
        return payment;
    }

    private void addMemberIfMissing(Project project, User user) {
        if (projectMemberRepository.findByProjectAndUser(project, user).isPresent()) {
            return;
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(ProjectMemberRole.MEMBER);
        projectMemberRepository.save(member);
    }

    private void cancelPendingJoinRequest(Project project, User user) {
        projectRequestRepository.findByProjectAndUser(project, user).ifPresent(req -> {
            if (req.getStatus() == ProjectRequestStatus.PENDING) {
                projectRequestRepository.delete(req);
            }
        });
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

    private String buildSuccessRedirect(UUID projectId, String returnTo) {
        String base = sanitizeReturnTo(returnTo);
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "payment=success&projectId=" + projectId;
    }

    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) {
            return "/projects";
        }
        if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/projects";
        }
        return returnTo;
    }
}
