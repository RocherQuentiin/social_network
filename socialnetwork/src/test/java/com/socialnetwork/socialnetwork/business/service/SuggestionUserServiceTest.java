package com.socialnetwork.socialnetwork.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.UUID;

import com.socialnetwork.socialnetwork.business.interfaces.service.IConnectionService;
import com.socialnetwork.socialnetwork.entity.Connection;
import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.IsepSpecialization;
import com.socialnetwork.socialnetwork.enums.UserGender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SuggestionUserServiceTest {

    @Mock
    private IConnectionService connectionService;

    @InjectMocks
    private SuggestionUserService suggestionService;

    private User mainUser;
    private User friendUser;
    private User candidateWithCommonFriend;
    private User candidateWithProfileMatch;

    @BeforeEach
    public void setup() {
        mainUser = createUser("alice");
        friendUser = createUser("bob");
        candidateWithCommonFriend = createUser("charlie");
        candidateWithProfileMatch = createUser("david");

        // set profiles
        Profile pMain = new Profile();
        pMain.setUser(mainUser);
        pMain.setIsepSpecialization(IsepSpecialization.SOFTWARE_ENGINEERING);
        pMain.setPromoYear((short)2024);
        Map<String,Object> interestsMain = new HashMap<>();
        interestsMain.put("i1","football");
        interestsMain.put("i2","coding");
        pMain.setInterests(interestsMain);
        Map<String,Object> compMain = new HashMap<>();
        compMain.put("c1","java");
        compMain.put("c2","spring");
        pMain.setCompetencies(compMain);
        mainUser.setProfile(pMain);

        Profile pCandidate = new Profile();
        pCandidate.setUser(candidateWithProfileMatch);
        pCandidate.setIsepSpecialization(IsepSpecialization.SOFTWARE_ENGINEERING);
        pCandidate.setPromoYear((short)2024);
        Map<String,Object> interestsCand = new HashMap<>();
        interestsCand.put("i1","coding");
        Map<String,Object> compCand = new HashMap<>();
        compCand.put("c1","java");
        pCandidate.setInterests(interestsCand);
        pCandidate.setCompetencies(compCand);
        candidateWithProfileMatch.setProfile(pCandidate);

        // friend user's profile (irrelevant)
        Profile pFriend = new Profile();
        pFriend.setUser(friendUser);
        friendUser.setProfile(pFriend);

        // candidateWithCommonFriend profile
        Profile pCommon = new Profile();
        pCommon.setUser(candidateWithCommonFriend);
        candidateWithCommonFriend.setProfile(pCommon);
    }

    private User createUser(String username) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setFirstName(username);
        u.setLastName("Tester");
        u.setEmail(username + "@example.com");
        return u;
    }

    private Connection makeConnection(User a, User b) {
        Connection c = new Connection();
        c.setId(UUID.randomUUID());
        c.setRequester(a);
        c.setReceiver(b);
        return c;
    }

    @Test
    public void returnsCandidateWhenCommonFriendExists() {
        // mainUser connected to friendUser
        Connection c1 = makeConnection(mainUser, friendUser);
        // candidate connected to friendUser
        Connection c2 = makeConnection(candidateWithCommonFriend, friendUser);

        when(connectionService.findAllAcceptedRequestByUserID(mainUser.getId())).thenReturn(Arrays.asList(c1));
        when(connectionService.findAllAcceptedRequestByUserID(candidateWithCommonFriend.getId())).thenReturn(Arrays.asList(c2));

        List<User> users = new ArrayList<>(Arrays.asList(mainUser, friendUser, candidateWithCommonFriend));
        Map<String,String> suggestions = suggestionService.getSuggestionUser(users, mainUser);

        // Should contain candidateWithCommonFriend
        boolean found = suggestions.keySet().stream().anyMatch(k -> k.contains(candidateWithCommonFriend.getId().toString().substring(0,8)) || k.contains(candidateWithCommonFriend.getLastName()) || k.contains(candidateWithCommonFriend.getFirstName()));
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.values().iterator().next()).contains("amis en commun");
    }

    @Test
    public void returnsCandidateWhenProfileMatches() {
        // mainUser has no direct connection to candidateWithProfileMatch
        when(connectionService.findAllAcceptedRequestByUserID(mainUser.getId())).thenReturn(Collections.emptyList());
        when(connectionService.findAllAcceptedRequestByUserID(candidateWithProfileMatch.getId())).thenReturn(Collections.emptyList());

        List<User> users = new ArrayList<>(Arrays.asList(mainUser, candidateWithProfileMatch));
        Map<String,String> suggestions = suggestionService.getSuggestionUser(users, mainUser);

        assertThat(suggestions).isNotEmpty();
        String msg = suggestions.values().iterator().next();
        assertThat(msg).contains("même spécialisation");
        assertThat(msg).contains("même promo");
        assertThat(msg).containsPattern("loisirs en commun|compétences en commun");
    }

    @Test
    public void returnsNoSuggestionWhenAlreadyConnected() {
        // mainUser connected to candidateWithProfileMatch -> should not be suggested
        Connection c = makeConnection(mainUser, candidateWithProfileMatch);
        when(connectionService.findAllAcceptedRequestByUserID(mainUser.getId())).thenReturn(Arrays.asList(c));

        List<User> users = new ArrayList<>(Arrays.asList(mainUser, candidateWithProfileMatch));
        Map<String,String> suggestions = suggestionService.getSuggestionUser(users, mainUser);

        assertThat(suggestions).isEmpty();
    }

    @Test
    public void findAllMatchBetweenTwoListCountsCorrectly() {
        // Prepare connections: common friend is friendUser
        Connection a1 = makeConnection(mainUser, friendUser);
        Connection a2 = makeConnection(mainUser, candidateWithCommonFriend);

        Connection b1 = makeConnection(candidateWithCommonFriend, friendUser);
        Connection b2 = makeConnection(candidateWithCommonFriend, createUser("other"));

        int common = suggestionService.findAllMatchBeetweenTwoList(Arrays.asList(a1,a2), mainUser, Arrays.asList(b1,b2), candidateWithCommonFriend);
        assertThat(common).isEqualTo(1);
    }

    @Test
    public void handlesNullProfilesGracefully() {
        // remove profiles
        mainUser.setProfile(null);
        candidateWithProfileMatch.setProfile(null);
        when(connectionService.findAllAcceptedRequestByUserID(mainUser.getId())).thenReturn(Collections.emptyList());
        when(connectionService.findAllAcceptedRequestByUserID(candidateWithProfileMatch.getId())).thenReturn(Collections.emptyList());

        List<User> users = new ArrayList<>(Arrays.asList(mainUser, candidateWithProfileMatch));
        Map<String,String> suggestions = suggestionService.getSuggestionUser(users, mainUser);

        // no exception, and no suggestions produced
        assertThat(suggestions).isEmpty();
    }
}
