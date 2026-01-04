package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPostService;
import com.socialnetwork.socialnetwork.entity.Post;

@Service
public class PostService implements IPostService{
	private IPostRepository repository;
	public PostService(IPostRepository repository) {
		this.repository = repository;
	}
	
	public ResponseEntity<List<Post>> getAllPostForConnectedUser(UUID userID){
		List<Post> listPost =  this.repository.findAllPostOfUser(userID);
		
		return new ResponseEntity<>(listPost, HttpStatus.OK);
		
	}
	
	public ResponseEntity<List<Post>> getAllPostVisibilityPublic(){
		List<Post> listPost =  this.repository.findByVisibilityPublic();
		
		return new ResponseEntity<>(listPost, HttpStatus.OK);
		
	}
}
