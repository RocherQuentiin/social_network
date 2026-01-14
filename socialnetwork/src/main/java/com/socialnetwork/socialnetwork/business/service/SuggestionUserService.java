package com.socialnetwork.socialnetwork.business.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.service.IConnectionService;
import com.socialnetwork.socialnetwork.business.interfaces.service.ISuggestionUserService;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class SuggestionUserService implements ISuggestionUserService{
	private IConnectionService connectionService;
	public SuggestionUserService(IConnectionService connectionService) {
		this.connectionService = connectionService;
	}
	
	@Override
	public HashMap<String, String> getSuggestionUser(List<User> listUsers, User user){
		HashMap<String, String> map = new HashMap<String, String>();
		listUsers.remove(user);
		for(int i = 0; i < listUsers.size(); i++) {
			String suggestion = "";
			
			List<Connection> userConnections = this.connectionService.findAllAcceptedRequestByUserID(user.getId());
			final int j = i;
			boolean match = userConnections.stream().anyMatch(x -> x.getReceiver().equals(listUsers.get(j)) || x.getRequester().equals(listUsers.get(j)));
			if(!match) {
				List<Connection> userConnectionsOfSuggestionUser = this.connectionService.findAllAcceptedRequestByUserID(listUsers.get(i).getId());
				List<Connection> common = new ArrayList<Connection>(userConnections);
		        common.retainAll(userConnectionsOfSuggestionUser);
				
				if(common.size() > 0) {
					suggestion += "Vous avez " + common.size() + " amis en commun";
				}
		        
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
			
			
			
		}
		
		return map;
	}
}
