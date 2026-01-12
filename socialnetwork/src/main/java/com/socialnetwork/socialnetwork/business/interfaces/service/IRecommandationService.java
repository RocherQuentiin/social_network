package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.socialnetwork.socialnetwork.entity.Recommendation;

public interface IRecommandationService {

	ResponseEntity<Recommendation> getRecommandationByUserIDAndRecommandedUserID(UUID fromString, UUID id);

	void saveRecommandation(Recommendation recommandationUser);

	ResponseEntity<List<Recommendation>> getAllRecommandationByRecommandedUser(UUID fromString);

}
