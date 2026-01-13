package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Recommendation;

public interface IRecommandationRepository extends JpaRepository<Recommendation, UUID>{

	Optional<Recommendation> findByUser_idAndRecommendedUser_id(UUID userID, UUID recommendedId);

	List<Recommendation> findByRecommendedUser_id(UUID userID);

}
