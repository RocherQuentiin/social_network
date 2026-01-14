package com.socialnetwork.socialnetwork.business.utils;

import java.util.HashMap;
import java.util.List;

import com.socialnetwork.socialnetwork.entity.User;

public class SuggestionUser {

	public static HashMap<String, String> getSuggestionUser(List<User> listUsers, User user){
		HashMap<String, String> map = new HashMap<String, String>();
		listUsers.remove(user);
		for(int i = 0; i < listUsers.size(); i++) {
			String suggestion = "";
			if(listUsers.get(i).getProfile().getIsepSpecialization() != null && user.getProfile().getIsepSpecialization() != null) {
				if(listUsers.get(i).getProfile().getIsepSpecialization().equals(user.getProfile().getIsepSpecialization())) {
					String specialization = user.getProfile().getIsepSpecialization().toString().replace('_', ' ');
					suggestion += "Vous faites partie de la même spécialisation : " + specialization;
				}
			}
			
			if(listUsers.get(i).getProfile().getPromoYear() != null && user.getProfile().getPromoYear() != null) {
				if(listUsers.get(i).getProfile().getPromoYear().equals(user.getProfile().getPromoYear())) {
					suggestion += "Vous faites partie de la même promo : " + user.getProfile().getPromoYear();
				}
			}
			
			
			
			if(!suggestion.equals("")) {
				map.put(listUsers.get(i).getId() + " " + listUsers.get(i).getLastName() + " " + listUsers.get(i).getFirstName(), suggestion);
			}
			
			
		}
		
		return map;
	}
}
