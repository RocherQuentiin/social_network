package com.socialnetwork.socialnetwork.dto;

import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;

public class UserProfileDto {

	private User user;
	
	private Profile profile;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
}
