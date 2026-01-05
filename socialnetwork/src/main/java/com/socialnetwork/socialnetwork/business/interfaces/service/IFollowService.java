package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Follow;

public interface IFollowService {

	ResponseEntity<Follow> getFollow(UUID userID, UUID userConnectedID);

	ResponseEntity<Follow> create(Follow followUser);


	ResponseEntity<Follow> delete(Follow follow);

}
