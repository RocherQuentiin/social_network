package com.socialnetwork.socialnetwork.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IMailService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPrivacySettingsService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProfileService;
import com.socialnetwork.socialnetwork.business.interfaces.service.ITokenService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.FileUpload;
import com.socialnetwork.socialnetwork.business.utils.Utils;

import com.socialnetwork.socialnetwork.dto.UserProfileDto;
import com.socialnetwork.socialnetwork.dto.UserRequestDto;
import com.socialnetwork.socialnetwork.entity.PrivacySettings;
import com.socialnetwork.socialnetwork.entity.Profile;
import com.socialnetwork.socialnetwork.entity.Post;

import com.socialnetwork.socialnetwork.entity.Token;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



@Controller
public class UserController {
	private final IUserService userService;
	private final IMailService mailService;
	private final ITokenService tokenService;
	private final IProfileService profileService;
	private final IPrivacySettingsService privacySettingsService;
	private final IUserRepository userRepository;
	private final IPostRepository postRepository;
	public UserController(IUserService userService, IMailService mailService, IPostRepository postRepository, ITokenService tokenService, IProfileService profileService, IPrivacySettingsService privacySettingsService) {
		this.userService = userService;
		this.mailService = mailService;
		this.tokenService = tokenService;
		this.profileService = profileService;
		this.privacySettingsService = privacySettingsService;
		this.postRepository = postRepository;
	}

    @GetMapping({"/", "/accueil"})
    public String showHomePage(HttpServletRequest request, Model model) {
    	HttpSession session = request.getSession(true);
		model.addAttribute("isConnect", session.getAttribute("userId"));
        return "accueil";
    }

	@GetMapping("/feed")
	public String showFeed(Model model, HttpServletRequest request) {
		model.addAttribute("name", "");
		// load posts ordered by createdAt desc
		List<Post> posts = postRepository.findAll();
		posts.sort(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
		model.addAttribute("posts", posts);

		// pass session existence to template (optional)
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("userId") != null) {
			model.addAttribute("loggedUserId", session.getAttribute("userId"));
			model.addAttribute("name", this.userService.getName(UUID.fromString(session.getAttribute("userId").toString())));
		}

		return "feed";
	}
    
	@GetMapping("/register")
	public String showRegisterForm(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, false);
		if(userIsConnect != null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		model.addAttribute("user", new User());
		return "register";
	}
	
	@GetMapping("/login")
	public String showLoginForm(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, false);
		if(userIsConnect != null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		model.addAttribute("user", new User());
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

			return "redirect:/feed";
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
			ResponseEntity<Profile> profileSave = this.profileService.create(userSave.getBody());
			ResponseEntity<PrivacySettings> privacySettingsSave = this.privacySettingsService.create(userSave.getBody());
			
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

	@PostMapping("/post")
	public String handleCreatePost(HttpServletRequest request, @RequestParam("content") String content, @RequestParam(value = "visibilityType", required = false) String visibilityTypeStr, @RequestParam(value = "allowComments", required = false) String[] allowCommentsValues) {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			return "redirect:/login";
		}
		try {
			UUID userId = UUID.fromString(session.getAttribute("userId").toString());
			User author = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
			Post post = new Post();
			post.setAuthor(author);
			post.setContent(content);
			if (visibilityTypeStr != null) {
				try {
					post.setVisibilityType(VisibilityType.valueOf(visibilityTypeStr));
				} catch (Exception e) {
					post.setVisibilityType(VisibilityType.PUBLIC);
				}
			} else {
				post.setVisibilityType(VisibilityType.PUBLIC);
			}
			boolean allowComments = false;
			if (allowCommentsValues != null) {
				allowComments = Arrays.stream(allowCommentsValues).anyMatch(v -> "true".equalsIgnoreCase(v));
			}
			post.setAllowComments(allowComments);
			postRepository.save(post);
		} catch (Exception e) {
			return "redirect:/accueil";
		}
		return "redirect:/accueil";
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
	public String showForgotPasswordMailForm(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, false);
		if(userIsConnect != null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		model.addAttribute("user", new User());
		return "emailForgotPassword";
	}
	
	@PostMapping("/forgotpassword/email")
	public String ForgotPasswordMailForm(HttpServletRequest request, User user, Model model) {
		ResponseEntity<User> existUser = this.userService.getUserByEmail(user.getEmail());
		
		if(existUser.getStatusCode() != HttpStatusCode.valueOf(200)) {
			model.addAttribute("error", "Utilisateur non existant");
			model.addAttribute("user", user);
			return "emailForgotPassword";
		}
		
		String code = UUID.randomUUID().toString();
		
		HttpSession session = request.getSession(true);
        session.setAttribute("userTokenId", existUser.getBody().getId());
        session.setAttribute("userEmail", existUser.getBody().getEmail());
       
        this.tokenService.create(code, existUser.getBody());
		
		this.mailService.sendForgotPassword(existUser.getBody().getEmail(), code, existUser.getBody().getFirstName());
		model.addAttribute("information", "Un mail permettant de modifier votre mot de passe a été envoyé sur votre adresse mail.");
		model.addAttribute("user", user);
		
		return "emailForgotPassword";
	}
	
	@GetMapping("/user/{code}/forgotpassword")
	public String showConfirmLinkPageForForgotPassword(HttpServletRequest request, @PathVariable("code") String code) {
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
		
		return "forgotpassword";
	}
	
	@PostMapping("/forgotpassword/changepassword")
	public String changePassword(HttpServletRequest request, Model model, @RequestParam("passwordHash") String passwordHash, @RequestParam("confirmpasswordHash") String confirmpasswordHash) {
		HttpSession session = request.getSession(false);
		
		if(session == null) {
			return "accueil";
		}

		Object userObject =   session.getAttribute("userTokenId");

		if(userObject == null) {
			return "accueil";
		}
		
		if(!passwordHash.equals(confirmpasswordHash)) {
			model.addAttribute("error", "Les deux mots de passes doivent être identiques");
			return "forgotpassword";
		}
		
		boolean passwordVerification = Utils.VerifyPassword(passwordHash);
		
		if(!passwordVerification) {
			model.addAttribute("error", "Le mot de passe doit contenir au moins 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
			return "forgotpassword";
		}

		String userID =   userObject.toString();
		
        this.userService.updatePassword(UUID.fromString(userID), passwordHash);
		
		session.setAttribute("userId", userID);
		
		session.removeAttribute("userTokenId");
		model.addAttribute("information", "Votre mot de passe à bien été modifié");
		
		return "forgotpassword";
	}
	
	@GetMapping("/changePassword")
	public String showChangePasswordForm(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, true);
		if(userIsConnect == null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		return "changePassword";
	}
	
	@PostMapping("/changePassword")
	public String changePassword(HttpServletRequest request, Model model, @RequestParam("oldpasswordHash") String oldpasswordHash, @RequestParam("passwordHash") String passwordHash, @RequestParam("confirmpasswordHash") String confirmpasswordHash) {
        HttpSession session = request.getSession(false);
		if(session == null) {
			return "accueil";
		}
		Object userObject =   session.getAttribute("userId");
		
		if(userObject == null) {
			return "accueil";
		}
		
		if(!passwordHash.equals(confirmpasswordHash)) {
			model.addAttribute("error", "Les deux mots de passes doivent être identiques");
			return "changePassword";
		}
		
		boolean passwordVerification = Utils.VerifyPassword(passwordHash);
		
		if(!passwordVerification) {
			model.addAttribute("error", "Le mot de passe doit contenir au moins 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
			return "changePassword";
		}
		
		String userID =   userObject.toString();
		
		ResponseEntity<User> user = this.userService.changePassword(UUID.fromString(userID), oldpasswordHash, confirmpasswordHash);
		
		if(user.getStatusCode() != HttpStatusCode.valueOf(200)) {
			model.addAttribute("error", "L'ancien mot de passe est incorrect");
			return "changePassword";
		}
		
		model.addAttribute("information", "Votre mot de passe a bien été modifié");
		
		return "changePassword";
	}
	
	@GetMapping("/logout")
	public String logOut(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession(false);
		
		if (session != null) {
            session.invalidate();
        }
		
		return "accueil";
	}
	
	@GetMapping("/profil")
	public String showUserProfil(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, true);
		if(userIsConnect == null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
		ResponseEntity<Profile> userProfile = this.profileService.getUserProfileByUserID(user.getBody());
		
		UserProfileDto userProfileDto = new UserProfileDto();
		userProfileDto.setUser(user.getBody());
		userProfileDto.setProfile(userProfile.getBody());

		model.addAttribute("userProfile", userProfileDto);
		return "userProfile";
	}
	
	@GetMapping("/editProfil")
	public String showEditUserProfil(HttpServletRequest request, Model model) {
		Object userIsConnect = Utils.validPage(request, true);
		if(userIsConnect == null) {
			model.addAttribute("isConnect", userIsConnect);
			return "accueil";
		}
		
		ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
		ResponseEntity<Profile> userProfile = this.profileService.getUserProfileByUserID(user.getBody());
		
		UserProfileDto userProfileDto = new UserProfileDto();
		userProfileDto.setUser(user.getBody());
		userProfileDto.setProfile(userProfile.getBody());

		model.addAttribute("userProfile", userProfileDto);
		model.addAttribute("isConnect", userIsConnect);
		return "editProfile";
	}
	
	@PostMapping("/editProfil")
	public String EditProfil(HttpServletRequest request, Model model, @ModelAttribute("userProfile") UserProfileDto userProfile,
            @RequestParam("profilePictureUrl") MultipartFile profilePicture,
            @RequestParam("coverPictureUrl") MultipartFile coverPicture) {
		
		Object userIsConnect = Utils.validPage(request, true);
		model.addAttribute("isConnect", userIsConnect);
		if(userIsConnect == null) {
			return "accueil";
		}
		if(userProfile.getUser().getFirstName().trim().length() == 0 || userProfile.getUser().getLastName().trim().length() == 0) {
			model.addAttribute("error", "Les champs marqué avec une etoile (*) sont obligatoire");
			return "editProfile";
		}
		String uploadProfilePictureUrl =  "";
		String uploadCoverPictureUrl = "";
		
		if(profilePicture != null && !profilePicture.isEmpty()) {
			System.out.println("profile picture : " + profilePicture);
			uploadProfilePictureUrl = FileUpload.UploadFile(profilePicture);
		}
		
		if(coverPicture != null && !coverPicture.isEmpty()) {
			System.out.println("profile picture : " + profilePicture);
			uploadCoverPictureUrl = FileUpload.UploadFile(coverPicture);
		}
		
		ResponseEntity<User> user = this.userService.updateUser(UUID.fromString(userIsConnect.toString()), userProfile.getUser(), uploadProfilePictureUrl, uploadCoverPictureUrl);
		ResponseEntity<Profile> profile = this.profileService.updateProfile(user.getBody(), userProfile.getProfile());
		
		UserProfileDto  userProfileDto = new UserProfileDto();
		userProfileDto.setUser(user.getBody());
		userProfileDto.setProfile(profile.getBody());
		model.addAttribute("information", "Vos informations ont bien été mise a jour");
		return "editProfile";
	}
}
