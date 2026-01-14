package com.socialnetwork.socialnetwork.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/projects")
public class ProjectPageController {

    private final IUserService userService;
    private final IProjectService projectService;

    public ProjectPageController(IUserService userService, IProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    /**
     * Display the projects management page
     * GET /projects
     */
    @GetMapping("")
    public String showProjectsPage(HttpServletRequest request, Model model) {
        // Check if user is authenticated
        Object userIsConnected = Utils.validPage(request, true);
        model.addAttribute("isConnect", userIsConnected);
        
        if (userIsConnected == null) {
            return "redirect:/login";
        }

        UUID userId = UUID.fromString(userIsConnected.toString());

                // --- CHARGEMENT INSTANTANÉ DES DONNÉES ---
                // On récupère les projets de l'utilisateur directement ici
                ResponseEntity<?> userProjectsResponse = projectService.getUserProjects(userId);
                model.addAttribute("projects", userProjectsResponse.getBody());

                // On récupère les projets publics aussi si nécessaire
                ResponseEntity<?> publicProjectsResponse = projectService.getPublicProjects();
                model.addAttribute("publicProjects", publicProjectsResponse.getBody());

                model.addAttribute("isConnect", userIsConnected);
                model.addAttribute("currentUserId", userIsConnected.toString());

                return "projects";
    }

    /**
     * Display public projects of a specific user
     * GET /projects/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public String showUserPublicProjects(@PathVariable("userId") UUID userId, 
                                        HttpServletRequest request, 
                                        Model model) {
        // Check if user is authenticated
        Object userIsConnected = Utils.validPage(request, true);
        model.addAttribute("isConnect", userIsConnected);
        
        if (userIsConnected == null) {
            return "redirect:/login";
        }

        // Get user information
        ResponseEntity<User> user = userService.getUserById(userId);
        if (user.getBody() == null) {
            return "redirect:/projects";
        }

        String userName = user.getBody().getFirstName() + " " + user.getBody().getLastName();
        model.addAttribute("userId", userId.toString());
        model.addAttribute("userName", userName);
        model.addAttribute("currentUserId", userIsConnected.toString());

        return "userPublicProjects";
    }
}
