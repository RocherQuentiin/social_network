package com.socialnetwork.socialnetwork.business.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPrivacySettingsRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPrivacySettingsService;
import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class PrivacySettingsService implements IPrivacySettingsService{

	private final IPrivacySettingsRepository repository;

    
	public PrivacySettingsService(IPrivacySettingsRepository repository) {
		this.repository = repository;
	}

	@Override
	public ResponseEntity<PrivacySettings> create(User user) {
		PrivacySettings privacySettings = new PrivacySettings();
		privacySettings.setUser(user);
		
		PrivacySettings savePrivacySettings = this.repository.save(privacySettings);
		
		return new ResponseEntity<>(
				savePrivacySettings, 
			      HttpStatus.OK);
	}
}
