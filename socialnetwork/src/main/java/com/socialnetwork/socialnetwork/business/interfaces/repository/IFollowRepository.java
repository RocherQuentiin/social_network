package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Follow;


public interface IFollowRepository extends JpaRepository<Follow, UUID> {
	Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
