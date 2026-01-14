package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;

public interface IProfileService {


	ResponseEntity<Profile> create(User user);

	ResponseEntity<Profile> getUserProfileByUserID(User user);

	ResponseEntity<Profile> updateProfile(User user, Profile profile);

	void save(Profile profile);

}
