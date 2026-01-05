package com.socialnetwork.socialnetwork.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.socialnetwork.business.interfaces.service.IFollowService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Follow;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class FollowController {
	private IFollowService followService;
	private IUserService userService;
	
	public FollowController(IFollowService followService, IUserService userService) {
		this.followService = followService;
		this.userService = userService;
	}
	
	@PostMapping("/follow")
	public ResponseEntity<String> followUser(HttpServletRequest request, @RequestParam("userID") String userID) {
		Object userIsConnect = Utils.validPage(request, true);
		
		 if (userIsConnect == null) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
	     }
		
		UUID UserFollowerUUID = UUID.fromString(userIsConnect.toString());

		UUID UserFollowingUUID = UUID.fromString(userID.toString());
		ResponseEntity<Follow> follow = this.followService.getFollow(UserFollowerUUID, UserFollowingUUID);
		
		if(follow.getStatusCode() == HttpStatusCode.valueOf(200)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Already exist");
		}
		
		ResponseEntity<User> userFollower =  this.userService.getUserById(UserFollowerUUID);
		ResponseEntity<User> userFollowing =  this.userService.getUserById(UserFollowingUUID);
		
		Follow followUser = new Follow();
		followUser.setFollower(userFollower.getBody());
		followUser.setFollowing(userFollowing.getBody());
		
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		
		followUser.setCreatedAt(now.toLocalDateTime());
		
		ResponseEntity<Follow> saveFollow = this.followService.create(followUser);
		
		return ResponseEntity.status(HttpStatus.OK).body("OK");
		
	}
	
	@PostMapping("/unfollow")
	public ResponseEntity<String> unfollowUser(HttpServletRequest request, @RequestParam("userID") String userID) {
		Object userIsConnect = Utils.validPage(request, true);
		
		 if (userIsConnect == null) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
	     }
		
		UUID UserFollowerUUID = UUID.fromString(userIsConnect.toString());

		UUID UserFollowingUUID = UUID.fromString(userID.toString());
		ResponseEntity<Follow> follow = this.followService.getFollow(UserFollowerUUID, UserFollowingUUID);
		
		if(follow.getStatusCode() != HttpStatusCode.valueOf(200)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not exist");
		}

		ResponseEntity<Follow> saveFollow = this.followService.delete(follow.getBody());
		
		return ResponseEntity.status(HttpStatus.OK).body("OK");
		
	}
}
