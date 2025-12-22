package com.socialnetwork.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.entity.User;



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
	public String registerUser( User user) {
		// In this simple example we accept the provided password directly as "passwordHash".
		// In production you must hash passwords before saving.
		userService.create(user);
		return "redirect:/users";
	}

	@GetMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userService.findAllUsers());
		return "users";
	}
}
