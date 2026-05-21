package com.socialnetwork.socialnetwork.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectPaymentService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProjectService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Project;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/projects")
public class ProjectPageController {

    private final IUserService userService;
    private final IProjectService projectService;
    private final IProjectPaymentService projectPaymentService;

    public ProjectPageController(IUserService userService, IProjectService projectService, IProjectPaymentService projectPaymentService) {
        this.userService = userService;
        this.projectService = projectService;
        this.projectPaymentService = projectPaymentService;
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

    @GetMapping("/{projectId}/payment")
    public String showProjectPaymentPage(@PathVariable("projectId") UUID projectId,
                                         @RequestParam(value = "returnTo", required = false) String returnTo,
                                         HttpServletRequest request,
                                         Model model) {
        Object userIsConnected = Utils.validPage(request, true);
        model.addAttribute("isConnect", userIsConnected);

        if (userIsConnected == null) {
            return "redirect:/login";
        }

        ResponseEntity<Project> projectResponse = projectService.getProjectById(projectId);
        Project project = projectResponse.getBody();
        if (projectResponse.getStatusCode().isError() || project == null || !Boolean.TRUE.equals(project.getIsPaid())) {
            return "redirect:/projects?payment=unavailable";
        }

        UUID currentUserId = UUID.fromString(userIsConnected.toString());
        if (project.getCreator() != null && project.getCreator().getId().equals(currentUserId)) {
            return "redirect:/projects?payment=own-project";
        }

        if (projectPaymentService.hasSuccessfulPayment(projectId, currentUserId)) {
            projectPaymentService.ensureMembershipAfterSuccessfulPayment(projectId, currentUserId);
            return "redirect:" + buildSuccessRedirect(projectId, returnTo);
        }

        model.addAttribute("project", project);
        model.addAttribute("currentUserId", currentUserId.toString());
        model.addAttribute("returnTo", sanitizeReturnTo(returnTo));
        ResponseEntity<User> currentUser = userService.getUserById(currentUserId);
        BigDecimal wallet = BigDecimal.ZERO;
        if (currentUser.getStatusCode().is2xxSuccessful() && currentUser.getBody() != null
                && currentUser.getBody().getWalletBalance() != null) {
            wallet = currentUser.getBody().getWalletBalance();
        }
        model.addAttribute("currentUserWalletBalance", wallet);
        return "projectPayment";
    }

    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) {
            return "/projects";
        }
        if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/projects";
        }
        return returnTo;
    }

    private String buildSuccessRedirect(UUID projectId, String returnTo) {
        String base = sanitizeReturnTo(returnTo);
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "payment=success&projectId=" + projectId;
    }
}
