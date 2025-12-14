package com.socialnetwork.socialnetwork.business.service;

import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;

@Service
public class UserService implements IUserService{

	@Override
	public String getMessage() {
		return "test";
	}
	
}
