package com.socialnetwork.socialnetwork.business.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.ITokenRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.ITokenService;
import com.socialnetwork.socialnetwork.entity.Token;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class TokenService implements ITokenService{

	private final ITokenRepository repository;

    
	public TokenService(ITokenRepository repository) {
		this.repository = repository;
	}
	
	public ResponseEntity<Token> create(String value, User user) {
		Token token = new Token();
		
		
		LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);
		
		token.setExpirationDate(nowPlusOneHour);
		token.setValue(value);
		token.setUser(user);
		
		Token saveToken = repository.save(token);
		
		return new ResponseEntity<>(
				saveToken, 
			      HttpStatus.OK);
		
	}
	
	public ResponseEntity<Token> getToken(UUID userID) {
		List<Token> listToken = this.repository.findByUser_Id(userID, Sort.by(Sort.Direction.DESC, "expirationDate"));
		
		if(listToken.size() == 0) {
			return new ResponseEntity<>(
					HttpStatus.NOT_FOUND);
		}
		
		System.out.println(listToken.size());
		System.out.println(listToken.get(0).getValue());
		
		return new ResponseEntity<>(
				listToken.get(0), 
			      HttpStatus.OK);
		
	}
}
