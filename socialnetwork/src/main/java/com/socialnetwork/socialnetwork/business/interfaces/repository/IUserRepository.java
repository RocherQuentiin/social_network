package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.socialnetwork.socialnetwork.entity.User;

import java.math.BigDecimal;

public interface IUserRepository extends JpaRepository<User, UUID>{

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);
	long countByIsActiveTrue();

	// find admin by role if needed
	List<User> findByRole(com.socialnetwork.socialnetwork.enums.UserRole role);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE User u SET u.walletBalance = :balance WHERE u.id = :userId")
	void updateWalletBalance(@Param("userId") UUID userId, @Param("balance") BigDecimal balance);

}
