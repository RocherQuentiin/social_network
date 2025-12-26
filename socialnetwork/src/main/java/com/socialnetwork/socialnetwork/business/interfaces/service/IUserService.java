package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.User;

public interface IUserService {
	String getName();

	ResponseEntity<User> create(User user);
	public ResponseEntity<User> getUser(User user);

	List<User> findAllUsers();
	
	ResponseEntity<User> update(UUID userID);

}