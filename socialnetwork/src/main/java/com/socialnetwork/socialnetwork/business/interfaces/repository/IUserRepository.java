package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.User;

public interface IUserRepository extends JpaRepository<User, UUID>{

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

}
