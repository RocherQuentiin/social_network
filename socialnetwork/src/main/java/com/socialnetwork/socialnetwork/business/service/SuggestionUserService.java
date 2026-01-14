package com.socialnetwork.socialnetwork.business.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import com.socialnetwork.socialnetwork.entity.Profile;

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
	
	public int findAllMatchBeetweenTwoList(List<Connection> userConnections, User user,  List<Connection> userConnectionsOfSuggestionUser, User userToSuggest) {
		List<User> users = new ArrayList<>();
		List<User> userSuggested = new ArrayList<>();
		for(int i = 0; i < userConnections.size(); i++) {
			if(!userConnections.get(i).getReceiver().equals(user)) {
				users.add(userConnections.get(i).getReceiver());
			}
			
			if(!userConnections.get(i).getRequester().equals(user)) {
				users.add(userConnections.get(i).getRequester());
			}
		}
		
		for(int i = 0; i < userConnectionsOfSuggestionUser.size(); i++) {
			if(!userConnectionsOfSuggestionUser.get(i).getReceiver().equals(userToSuggest)) {
				userSuggested.add(userConnectionsOfSuggestionUser.get(i).getReceiver());
			}
			
			if(!userConnectionsOfSuggestionUser.get(i).getRequester().equals(userToSuggest)) {
				userSuggested.add(userConnectionsOfSuggestionUser.get(i).getRequester());
			}
		}
		
		List<User> common = new ArrayList<>(users);
        common.retainAll(userSuggested);
		
		return common.size();
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
				int cptCommonFriends = findAllMatchBeetweenTwoList(userConnections, user, userConnectionsOfSuggestionUser, listUsers.get(i));
				if (cptCommonFriends > 0) {
					suggestion += "\n Vous avez " + cptCommonFriends + " amis en commun";
				}

				// Profiles may be null; access fields only when present
				Profile pUser = user.getProfile();
				Profile pCand = listUsers.get(i).getProfile();
				if(pUser != null && pCand != null) {
					if(pCand.getIsepSpecialization() != null && pUser.getIsepSpecialization() != null) {
						if(pCand.getIsepSpecialization().equals(pUser.getIsepSpecialization())) {
							String specialization = pUser.getIsepSpecialization().toString().replace('_', ' ');
							suggestion += "\n Vous faites partie de la même spécialisation : " + specialization;
						}
					}
					if(pCand.getPromoYear() != null && pUser.getPromoYear() != null) {
						if(pCand.getPromoYear().equals(pUser.getPromoYear())) {
							suggestion += "\n Vous faites partie de la même promo : " + pUser.getPromoYear();
						}
					}
					if(pUser.getInterests() != null && pCand.getInterests() != null) {
						Set<Object> commonValues = new HashSet<>(pUser.getInterests().values());
						commonValues.retainAll(pCand.getInterests().values());
						if(commonValues.size() > 0) {
							suggestion += "\n Vous avez " + commonValues.size() + " loisirs en commun";
						}
					}
					if(pUser.getCompetencies() != null && pCand.getCompetencies() != null) {
						Set<Object> commonValues = new HashSet<>(pUser.getCompetencies().values());
						commonValues.retainAll(pCand.getCompetencies().values());
						if(commonValues.size() > 0) {
							suggestion += "\n Vous avez " + commonValues.size() + " compétences en commun";
						}
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
