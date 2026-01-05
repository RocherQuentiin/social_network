package com.socialnetwork.socialnetwork.business.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IMediaRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IMediaService;
import com.socialnetwork.socialnetwork.entity.Media;

@Service
public class MediaService implements IMediaService{

	private IMediaRepository repositoy;
	
	public MediaService(IMediaRepository repository) {
		this.repositoy = repository;
	}

	@Override
	public ResponseEntity<Media> create(Media media) {
		Media saveMedia = this.repositoy.save(media);
		
		
		return new ResponseEntity<>(
				  saveMedia, 
			      HttpStatus.OK);
	}
}
