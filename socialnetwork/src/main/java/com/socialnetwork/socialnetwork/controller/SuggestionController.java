package com.socialnetwork.socialnetwork.controller;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.ISuggestionUserService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.service.SuggestionUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/suggestion")
public class SuggestionController {

	private IUserService userService;
	private ISuggestionUserService suggestionService;
	
	public SuggestionController(IUserService userService, ISuggestionUserService suggestionService) {
		this.userService = userService;
		this.suggestionService = suggestionService;
	}
	
	@GetMapping("")
    public ResponseEntity<HashMap<String, String>> getPendingRequests(HttpServletRequest request) {
        Object userIsConnect = Utils.validPage(request, true);
        if (userIsConnect == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<User> users = this.userService.findAllUsers();
        ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
        
        if(user.getStatusCode() != HttpStatusCode.valueOf(200)) {
        	return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return new ResponseEntity<>(
        		  this.suggestionService.getSuggestionUser(users, user.getBody()), 
			      HttpStatus.OK);
    }
}
