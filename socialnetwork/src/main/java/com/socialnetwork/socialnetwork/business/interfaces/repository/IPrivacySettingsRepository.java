package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;



public interface IPrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID>{

	Optional<PrivacySettings> findByUser(User user);

}
