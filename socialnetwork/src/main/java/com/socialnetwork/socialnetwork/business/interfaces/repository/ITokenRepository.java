package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Token;


public interface ITokenRepository extends JpaRepository<Token, UUID>{
	List<Token> findByUser_Id(UUID userId, Sort sort);
}
