package com.socialnetwork.socialnetwork.dto;

import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;

public class UserProfileDto {

	private User user;
	
	private Profile profil;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Profile getProfil() {
		return profil;
	}

	public void setProfil(Profile profil) {
		this.profil = profil;
	}
}
