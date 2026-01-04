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
import com.socialnetwork.socialnetwork.enums.UserGender;

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
	
	@Override
	public ResponseEntity<Profile> updateProfile(User user, Profile profile) {
		Optional<Profile> existingProfile = this.repository.findByUser(user);
		System.out.println("profile : " + existingProfile.isPresent());
		System.out.println(user.getId());
		if(existingProfile.isPresent()) {
			System.out.println("company : " + profile.getCompany());
			existingProfile.get().setBirthdate(profile.getBirthdate());
			existingProfile.get().setCompany(profile.getCompany());
			existingProfile.get().setUserGender(profile.getUserGender());
			existingProfile.get().setLocation(profile.getLocation());
			existingProfile.get().setPhoneNumber(profile.getPhoneNumber());
			existingProfile.get().setEducation(profile.getEducation());
			existingProfile.get().setIsepSpecialization(profile.getIsepSpecialization());
			existingProfile.get().setPromoYear(profile.getPromoYear());
			existingProfile.get().setWebsite(profile.getWebsite());
			existingProfile.get().setProfession(profile.getProfession());
			repository.save(existingProfile.get());
		}
		
		
		return new ResponseEntity<>(
			      HttpStatus.OK);
	}
	
}
