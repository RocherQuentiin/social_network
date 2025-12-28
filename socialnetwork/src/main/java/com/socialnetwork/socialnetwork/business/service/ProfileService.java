package com.socialnetwork.socialnetwork.business.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IProfileRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProfileService;
import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class ProfileService implements IProfileService{

    private final IProfileRepository repository;

    
	public ProfileService(IProfileRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public ResponseEntity<Profile> create(User user) {
		Profile profile = new Profile();
		profile.setUser(user);
		
		Profile saveProfile = this.repository.save(profile);
		
		return new ResponseEntity<>(
				saveProfile, 
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Profile> getUserProfileByUserID(User user) {
		Optional<Profile> profile = this.repository.findByUser(user);
		
		if(!profile.isPresent()) {
			return new ResponseEntity<>(
				      HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(
				profile.get(), 
			      HttpStatus.OK);
	}
	
}
