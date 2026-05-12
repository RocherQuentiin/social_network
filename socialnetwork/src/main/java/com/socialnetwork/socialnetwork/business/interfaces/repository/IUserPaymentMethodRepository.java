package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.entity.UserPaymentMethod;

public interface IUserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, UUID> {

    List<UserPaymentMethod> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<UserPaymentMethod> findByIdAndUser_Id(UUID id, UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPaymentMethod u WHERE u.id = :id AND u.user.id = :userId")
    int deleteOwned(@Param("id") UUID id, @Param("userId") UUID userId);
}
