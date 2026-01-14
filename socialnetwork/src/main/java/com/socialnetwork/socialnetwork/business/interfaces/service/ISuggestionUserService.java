package com.socialnetwork.socialnetwork.business.interfaces.service;

import java.util.HashMap;
import java.util.List;

import com.socialnetwork.socialnetwork.entity.User;

public interface ISuggestionUserService {

	HashMap<String, String> getSuggestionUser(List<User> listUsers, User user);

}
