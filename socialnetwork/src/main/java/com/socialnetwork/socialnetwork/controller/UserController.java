package com.socialnetwork.socialnetwork.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialnetwork.socialnetwork.business.interfaces.service.IMailService;
import com.socialnetwork.socialnetwork.business.interfaces.service.ITokenService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.service.MailService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.entity.Token;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



@Controller
public class UserController {
	private final IUserService userService;
	private final IMailService mailService;
	private final ITokenService tokenService;
	public UserController(IUserService userService, IMailService mailService, ITokenService tokenService) {
		this.userService = userService;
		this.mailService = mailService;
		this.tokenService = tokenService;
	}

    @GetMapping({"/", "/accueil"})
    public String showHomePage(Model model) {
		model.addAttribute("name", this.userService.getName());
        return "accueil";
    }
    
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}
	
	@GetMapping("/login")
	public String showLoginForm(Model model) {
		model.addAttribute("user", new User());
		System.out.println("ok showLoginForm");
		return "login";
	}
	
	@PostMapping("/login")
	public String loginUser(HttpServletRequest request, User user, Model model) {
		ResponseEntity<User> userLogin = userService.getUser(user);
		
		if(userLogin.getStatusCode() == HttpStatusCode.valueOf(404)) {
			model.addAttribute("error", "Email ou le Mot de passe incorrect");
			model.addAttribute("user", user);
			return "login";
		}
		
		else if(!userLogin.getBody().getIsVerified()) {
            String code = UUID.randomUUID().toString();
			
			HttpSession session = request.getSession(true);
            session.setAttribute("userTokenId", userLogin.getBody().getId());
            session.setAttribute("userEmail", userLogin.getBody().getEmail());
            
            this.tokenService.create(code, userLogin.getBody());
			
			this.mailService.sendConfirmationAccountMail(userLogin.getBody().getEmail(), code, userLogin.getBody().getFirstName());
			
			model.addAttribute("information", "Un mail de confirmation de création de compte à était envoyé sur votre adresse mail.");
			model.addAttribute("user", user);

			return "login";
		}
		
		else {
			HttpSession session = request.getSession(true);
            session.setAttribute("userId", userLogin.getBody().getId());
            session.setAttribute("userEmail", userLogin.getBody().getEmail());

            return "redirect:/accueil";
		}
	}

	@PostMapping("/register")
	public String registerUser(HttpServletRequest request, User user, Model model) {
		// email domain validation for ISEP
		String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";

		Pattern studentPattern = Pattern.compile("^[A-Za-z0-9._%+-]+\\@eleve\\.isep\\.fr$");
		Pattern profPattern = Pattern.compile("^[A-Za-z0-9._%+-]+\\@(isep\\.fr|ext\\.isep\\.fr)$");

		if (studentPattern.matcher(email).matches()) {
			user.setRole(UserRole.STUDENT);
		} else if (profPattern.matcher(email).matches()) {
			user.setRole(UserRole.PROF);
		} else {
			model.addAttribute("error", "L'email doit être une adresse ISEP (eleve.isep.fr, isep.fr, ext.isep.fr)");
			model.addAttribute("user", user);
			return "register";
		}
		
		boolean passwordVerification = Utils.VerifyPassword(user.getPasswordHash());
		
		if(!passwordVerification) {
			model.addAttribute("error", "Le mot de passe doit contenir au moins 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
			model.addAttribute("user", user);
			return "register";
		}

		try {
			ResponseEntity<User> userSave = userService.create(user);
			
			if(userSave.getStatusCode() != HttpStatusCode.valueOf(200)) {
				model.addAttribute("error", "Utilisateur déja existant");
				model.addAttribute("user", user);
				return "register";
			}
			
			String code = UUID.randomUUID().toString();
			
			HttpSession session = request.getSession(true);
            session.setAttribute("userTokenId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
           
            this.tokenService.create(code, userSave.getBody());
			
			this.mailService.sendConfirmationAccountMail(email, code, user.getFirstName());
			model.addAttribute("information", "Un mail de confirmation de création de compte à était envoyé sur votre adresse mail.");
			model.addAttribute("user", user);

			return "register";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("user", user);
			return "register";
		}
	}
	
	
	@GetMapping("/user/{code}/confirm")
	public String showConfirmLinkPage(HttpServletRequest request, @PathVariable("code") String code) {
		HttpSession session = request.getSession(false);
		
		if(session == null) {
			return "accueil";
		}
		
		Object userObject =   session.getAttribute("userTokenId");

		if(userObject == null) {
			return "accueil";
		}
		
		String userID =   userObject.toString();
		
		ResponseEntity<Token> token = this.tokenService.getToken(UUID.fromString(userID));
		if(token.getStatusCode() != HttpStatusCode.valueOf(200)) {
			return "accueil";
		}
		
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		if(!token.getBody().getValue().equals(code) || token.getBody().getExpirationDate().isBefore(now.toLocalDateTime())) {
			return "accueil";
		}
		this.userService.update(UUID.fromString(userID));
		
		session.setAttribute("userId", userID);
		
		session.removeAttribute("userTokenId");
		
		return "confirmRegister";
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
	
	@GetMapping("/forgotpassword/email")
	public String showForgotPasswordMailForm(Model model) {
		System.out.println("ok");
		return "emailForgotPassword";
	}
	
	@PostMapping("/forgotpassword/email")
	public String ForgotPasswordMailForm(HttpServletRequest request, String email) {
		return "emailForgotPassword";
	}
}
