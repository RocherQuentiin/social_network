package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService implements IUserService{
	private final IUserRepository repository;
	private final PasswordEncoder passwordEncoder;
    
	public UserService(IUserRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}
    
	@Override
	public String getName() {
		return repository.findAll()
				.stream()
				.findFirst()
				.map(User::getUsername)
				.orElse("");
	}

	@Override
	public User create(User user) {
		if (user.getUsername() != null && repository.findByUsername(user.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username already exists");
		}

		if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
			user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
		}

		return repository.save(user);
	}
	
	@Override
	public ResponseEntity<User> getUser(User user) {
		Optional<User> userLogin = null;
		System.out.println(user.getEmail());
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
	public List<User> findAllUsers() {
		return repository.findAll();
	}
    
}
