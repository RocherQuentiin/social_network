package com.socialnetwork.socialnetwork.business.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class UserService implements IUserService{
	private final IUserRepository repository;
	
	public UserService(IUserRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public String getName() {
		Optional<User> user = this.repository.findById(1);
		String name = user.isPresent() ? user.get().getName() : "";
		return name;
	}
	
}
