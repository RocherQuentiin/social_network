package com.socialnetwork.socialnetwork.business.interfaces.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnetwork.socialnetwork.entity.User;

public interface IUserRepository extends JpaRepository<User, Integer>{

}
