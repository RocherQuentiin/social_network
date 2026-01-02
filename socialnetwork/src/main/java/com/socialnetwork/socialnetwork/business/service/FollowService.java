package com.socialnetwork.socialnetwork.business.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IFollowRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IFollowService;
import com.socialnetwork.socialnetwork.entity.Follow;

@Service
public class FollowService implements IFollowService{

	private IFollowRepository repository;
	public FollowService(IFollowRepository followRepository) {
		this.repository = followRepository;
	}
	
	@Override
	public ResponseEntity<Follow> getFollow(UUID userID, UUID userFollowedID) {
		Optional<Follow> follow = this.repository.findByFollowerIdAndFollowingId(userID, userFollowedID);
		
		if(!follow.isPresent()) {
			return new ResponseEntity<>(
				      HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(
				follow.get(), 
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Follow> create(Follow followUser) {
		Follow saveFollow = this.repository.save(followUser);
		
		return new ResponseEntity<>(
				saveFollow, 
			      HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Follow> delete(Follow follow) {
		this.repository.delete(follow);
		
		return new ResponseEntity<>(
			      HttpStatus.OK);
	}
}
