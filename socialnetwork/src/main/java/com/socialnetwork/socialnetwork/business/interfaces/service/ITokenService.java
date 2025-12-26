package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;


import com.socialnetwork.socialnetwork.entity.Token;
import com.socialnetwork.socialnetwork.entity.User;

public interface ITokenService {
	public ResponseEntity<Token> create(String value, User user);
	public ResponseEntity<Token> getToken(UUID userID);
}
