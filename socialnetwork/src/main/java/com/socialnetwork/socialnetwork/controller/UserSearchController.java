package com.socialnetwork.socialnetwork.controller;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserSearchController {
    
    @Autowired
    private IUserService userService;
    
    /**
     * Search users by username, email, or name
     * @param search Search query (minimum 2 characters)
     * @return List of matching users
     */
    @GetMapping
    public ResponseEntity<List<User>> searchUsers(@RequestParam(value = "search", required = false) String search) {
        try {
            if (search == null || search.trim().isEmpty() || search.length() < 2) {
                return ResponseEntity.ok(List.of());
            }
            
            String query = search.trim().toLowerCase();
            
            // Get all users and filter by search criteria
            List<User> allUsers = userService.findAllUsers();
            List<User> filteredUsers = allUsers.stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(query) ||
                    user.getEmail().toLowerCase().contains(query) ||
                    (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(query)) ||
                    (user.getLastName() != null && user.getLastName().toLowerCase().contains(query))
                )
                .limit(20) // Limit results to 20
                .toList();
            
            System.out.println("Search query: " + query + " - Found: " + filteredUsers.size() + " users");
            return ResponseEntity.ok(filteredUsers);
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
}
