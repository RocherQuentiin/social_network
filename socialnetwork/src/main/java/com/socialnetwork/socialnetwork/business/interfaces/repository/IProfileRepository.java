package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;

public interface IProfileRepository  extends JpaRepository<Profile, UUID>{
	Optional<Profile> findByUser(User user);
}
