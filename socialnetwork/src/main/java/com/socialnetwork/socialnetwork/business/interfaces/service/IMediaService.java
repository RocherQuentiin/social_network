package com.socialnetwork.socialnetwork.business.interfaces.service;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Media;

public interface IMediaService {
	ResponseEntity<Media>  create(Media media);

}
