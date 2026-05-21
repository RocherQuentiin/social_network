package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.ProjectPayment;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.PaymentStatus;

public interface IProjectPaymentRepository extends JpaRepository<ProjectPayment, UUID> {

    /**
     * Paiement SUCCESS encore valide (non remboursé). Après remboursement à la sortie du projet,
     * l'utilisateur doit repayer avec une nouvelle saisie carte.
     */
    @Query("""
            SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END
            FROM ProjectPayment pp
            WHERE pp.project = :project AND pp.payer = :payer AND pp.status = :status
              AND (pp.refunded = false OR pp.refunded IS NULL)
            """)
    boolean existsSuccessfulPaymentNotRefunded(@Param("project") Project project,
                                               @Param("payer") User payer,
                                               @Param("status") PaymentStatus status);

    Optional<ProjectPayment> findTopByProjectAndPayerAndStatusOrderByCreatedAtDesc(Project project, User payer, PaymentStatus status);

    @Query("""
            SELECT COALESCE(SUM(pp.amount), 0)
            FROM ProjectPayment pp
            WHERE pp.project.creator.id = :creatorId AND pp.status = :status
              AND (pp.refunded = false OR pp.refunded IS NULL)
            """)
    BigDecimal sumRevenueByCreatorAndStatus(@Param("creatorId") UUID creatorId, @Param("status") PaymentStatus status);

    List<ProjectPayment> findByProjectAndStatusAndRefunded(Project project, PaymentStatus status, boolean refunded);

    List<ProjectPayment> findByProjectAndStatusAndPayerAndRefunded(Project project, PaymentStatus status, User payer, boolean refunded);
}
