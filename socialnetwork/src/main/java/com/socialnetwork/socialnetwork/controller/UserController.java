package com.socialnetwork.socialnetwork.controller;

import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;



@Controller
public class UserController {
	private final IUserService userService;
	public UserController(IUserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/")
    public String showHomePage(Model model) {
		model.addAttribute("name", this.userService.getName());
        return "index";
    }
    
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(User user, Model model) {
		// email domain validation for ISEP
		String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";

		Pattern studentPattern = Pattern.compile("^[A-Za-z0-9._%+-]+\\@eleve\\.isep\\.fr$");
		Pattern profPattern = Pattern.compile("^[A-Za-z0-9._%+-]+\\@(isep\\.fr|ext\\.isep\\.fr)$");

		if (studentPattern.matcher(email).matches()) {
			user.setRole(UserRole.STUDENT);
		} else if (profPattern.matcher(email).matches()) {
			user.setRole(UserRole.PROF);
		} else {
			model.addAttribute("error", "Registration is restricted to ISEP email addresses.");
			model.addAttribute("user", user);
			return "register";
		}
		
		boolean passwordVerification = Utils.VerifyPassword(user.getPasswordHash());
		
		if(!passwordVerification) {
			model.addAttribute("error", "Password must contains at least 8 characters, contains at least one minuscule, contains at least one majuscule, contains at least one number, contains at least one special Character");
			model.addAttribute("user", user);
			return "register";
		}

		try {
			userService.create(user);
			return "redirect:/users";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("user", user);
			return "register";
		}
	}

	@GetMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userService.findAllUsers());
		return "users";
	}

	@GetMapping("/api/check-username")
	@ResponseBody
	public ResponseEntity<?> checkUsername(@RequestParam("username") String username) {
		boolean exists = false;
		if (username != null && !username.isBlank()) {
			exists = userService.findAllUsers().stream()
					.anyMatch(u -> username.equalsIgnoreCase(u.getUsername()));
		}
		return ResponseEntity.ok(java.util.Map.of("exists", exists));
	}
}
