package com.socialnetwork.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;



@Controller
public class WebController {
	private final IUserService userService;
	public WebController(IUserService userService) {
		this.userService = userService;
	}
	
	@GetMapping("/")
    public String showHomePage(Model model) {
		model.addAttribute("name", this.userService.getMessage());
        return "index";
    }
}
