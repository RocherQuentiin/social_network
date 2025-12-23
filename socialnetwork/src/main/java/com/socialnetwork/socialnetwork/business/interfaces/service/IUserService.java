package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;

import com.socialnetwork.socialnetwork.entity.User;

public interface IUserService {
	String getName();

	User create(User user);
	public User getUser(User user);

	List<User> findAllUsers();

}