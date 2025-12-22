package com.socialnetwork.socialnetwork.business.service;

import java.util.List;

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
	public String getName() {
		return repository.findAll()
				.stream()
				.findFirst()
				.map(User::getUsername)
				.orElse("");
	}

	@Override
	public User create(User user) {
		if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
			user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
		}
		return repository.save(user);
	}

	@Override
	public List<User> findAllUsers() {
		return repository.findAll();
	}
    
}
