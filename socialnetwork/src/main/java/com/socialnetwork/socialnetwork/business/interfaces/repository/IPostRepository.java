package com.socialnetwork.socialnetwork.business.interfaces.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.Post;

public interface IPostRepository extends JpaRepository<Post, UUID> {

}
