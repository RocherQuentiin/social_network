package com.socialnetwork.socialnetwork.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/messages")
public class MessagingPageController {
    
    @GetMapping
    public String messagingPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        Object userId = session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("isConnect", userId);
        model.addAttribute("userId", userId.toString());
        
        return "messages";
    }
}
