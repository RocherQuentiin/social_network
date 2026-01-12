package com.socialnetwork.socialnetwork.business.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IRecommandationRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IRecommandationService;
import com.socialnetwork.socialnetwork.entity.Recommendation;

@Service
public class RecommandationService implements IRecommandationService {
	private IRecommandationRepository repository;

	public RecommandationService(IRecommandationRepository repository) {
		this.repository = repository;
	}

	@Override
	public ResponseEntity<Recommendation> getRecommandationByUserIDAndRecommandedUserID(UUID userID, UUID recommendedId) {
		Optional<Recommendation> recommandation = this.repository.findByUser_idAndRecommendedUser_id(userID, recommendedId);
		
		if(!recommandation.isPresent()) {
			return new ResponseEntity<>(
				      HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(
				recommandation.get(), 
			      HttpStatus.OK);
	}

	@Override
	public void saveRecommandation(Recommendation recommandationUser) {
		this.repository.save(recommandationUser);
	}

	@Override
	public ResponseEntity<List<Recommendation>> getAllRecommandationByRecommandedUser(UUID userID) {
		List<Recommendation> listRecommandation = this.repository.findByRecommendedUser_id(userID);
		
		
		return new ResponseEntity<>(
				listRecommandation, 
			      HttpStatus.OK);
	}
	
	
	
}
