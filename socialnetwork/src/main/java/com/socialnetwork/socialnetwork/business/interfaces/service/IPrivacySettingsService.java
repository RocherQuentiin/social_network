package com.socialnetwork.socialnetwork.business.interfaces.service;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;

public interface IPrivacySettingsService {

	ResponseEntity<PrivacySettings> create(User user);

}
