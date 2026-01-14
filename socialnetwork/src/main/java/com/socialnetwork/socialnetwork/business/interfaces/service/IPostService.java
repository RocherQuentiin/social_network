package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Post;

public interface IPostService {
	public ResponseEntity<List<Post>> getAllPostVisibilityPublic();
	public ResponseEntity<List<Post>> getAllPostForConnectedUser(UUID userID);
	public ResponseEntity<Post> createPost(Post post);
}
