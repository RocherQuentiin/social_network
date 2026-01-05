package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;

public interface IPrivacySettingsService {

	ResponseEntity<PrivacySettings> create(User user);

	ResponseEntity<PrivacySettings> getPrivacySettingsByUser(User user);

	ResponseEntity<PrivacySettings> getPrivacySettingsByUserID(UUID userID);

	ResponseEntity<PrivacySettings> savePrivacy(PrivacySettings privacySettings);
}
