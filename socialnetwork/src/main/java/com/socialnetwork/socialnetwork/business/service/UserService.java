package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class UserService implements IUserService{
	private final IUserRepository repository;
	private final PasswordEncoder passwordEncoder;
    
	public UserService(IUserRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}
	

	@Override
	public String getName(UUID userId) {
		return repository.findById(userId)
				.map(User::getUsername)
				.orElse("");
	}

	
	@Override
	public ResponseEntity<User> getUserByEmail(String email) {
		Optional<User> user = repository.findByEmail(email);
		if (email != "" && !user.isPresent()) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<User>(user.get(), HttpStatus.OK);
	}
	

	@Override
	public ResponseEntity<User> create(User user) {
		if (user.getUsername() != null && repository.findByUsername(user.getUsername()).isPresent()) {
			return new ResponseEntity<User>(HttpStatus.CONFLICT);
		}
		
		if (user.getEmail() != null && repository.findByEmail(user.getEmail()).isPresent()) {
			return new ResponseEntity<User>(HttpStatus.CONFLICT);
		}

		if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
			user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
		}

		User saveUser = repository.save(user);
		
		return new ResponseEntity<>(
			      saveUser, 
			      HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<User> getUser(User user) {
		Optional<User> userLogin = null;

		if (user.getEmail() != null) {
			userLogin = repository.findByEmail(user.getEmail());
			if(!userLogin.isPresent()) {
				return new ResponseEntity<User>(
					      HttpStatus.NOT_FOUND);
			}	
		}

		if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
			boolean passwordVeriy = passwordEncoder.matches(user.getPasswordHash(), userLogin.get().getPasswordHash());
			
			if(!passwordVeriy) {
				return new ResponseEntity<User>(
					      HttpStatus.NOT_FOUND);
			}
		}

		return new ResponseEntity<>(
			      userLogin.get(), 
			      HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<User> changePassword(UUID userId, String oldPassword, String newPassword) {
		Optional<User> user = repository.findById(userId);
		
		boolean passwordVeriy = passwordEncoder.matches(oldPassword, user.get().getPasswordHash());
		
		if(!passwordVeriy) {
			return new ResponseEntity<User>(
				      HttpStatus.CONFLICT);
		}
		
		user.get().setPasswordHash(passwordEncoder.encode(newPassword));
		
		repository.save(user.get());

		return new ResponseEntity<>(
				  user.get(), 
			      HttpStatus.OK);
	}

	@Override
	public List<User> findAllUsers() {
		return repository.findAll();
	}

	@Override
	public ResponseEntity<User> update(UUID userID) {
		Optional<User> existingUser = repository.findById(userID);
		
		if(existingUser.isPresent()) {
			existingUser.get().setIsVerified(true);
			
			repository.save(existingUser.get());
		}
		
		
		return new ResponseEntity<>(
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<User> updatePassword(UUID userID, String password) {
		Optional<User> existingUser = repository.findById(userID);
		
		if(existingUser.isPresent()) {
			existingUser.get().setPasswordHash(passwordEncoder.encode(password));
			
			repository.save(existingUser.get());
		}
		
		
		return new ResponseEntity<>(
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<User> getUserById(UUID userID) {
		Optional<User> user = repository.findById(userID);

		if(user.isPresent()) {
			 return new ResponseEntity<>(user.get(),
				      HttpStatus.OK);
		}
		return new ResponseEntity<User>(
			      HttpStatus.NOT_FOUND);
	}
	
	@Override
	public ResponseEntity<User> updateUser(UUID userID, User user, String uploadProfilePictureUrl, String uploadCoverPictureUrl) {
		Optional<User> existingUser = repository.findById(userID);
		System.out.println(existingUser.isPresent());
		if(existingUser.isPresent()) {
			existingUser.get().setFirstName(user.getFirstName());
			existingUser.get().setLastName(user.getLastName());
			existingUser.get().setBio(user.getBio());
			if(!uploadProfilePictureUrl.equals("")) {
				existingUser.get().setProfilePictureUrl(uploadProfilePictureUrl);
			}
			
			if(!uploadCoverPictureUrl.equals("")) {
				existingUser.get().setCoverPictureUrl(uploadCoverPictureUrl);
			}
			
			
			User userSave = repository.save(existingUser.get());
			return new ResponseEntity<>(userSave,
				      HttpStatus.OK);
		}
		return new ResponseEntity<>(
			      HttpStatus.NOT_FOUND);
	}
    
}
