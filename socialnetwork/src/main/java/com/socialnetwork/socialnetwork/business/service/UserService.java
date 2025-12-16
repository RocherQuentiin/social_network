package com.socialnetwork.socialnetwork.business.service;

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
	public String getMessage() {
		System.out.println("ok");
		User user = this.repository.findById(1).get();
		return user.getName();
	}
	
}
