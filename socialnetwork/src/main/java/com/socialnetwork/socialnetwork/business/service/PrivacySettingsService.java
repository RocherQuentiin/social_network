package com.socialnetwork.socialnetwork.business.service;

import java.util.Optional;

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
	
	@Override
	public ResponseEntity<PrivacySettings> getPrivacySettingsByUser(User user) {
		Optional<PrivacySettings> privacySettings = this.repository.findByUser(user);
		
		if(!privacySettings.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(
				privacySettings.get(), 
			      HttpStatus.OK);
	}
}
