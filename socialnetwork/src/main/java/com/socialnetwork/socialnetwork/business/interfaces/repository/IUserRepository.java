package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.User;

public interface IUserRepository extends JpaRepository<User, UUID>{

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	long countByIsActiveTrue();

	// find admin by role if needed
	List<User> findByRole(com.socialnetwork.socialnetwork.enums.UserRole role);

}
