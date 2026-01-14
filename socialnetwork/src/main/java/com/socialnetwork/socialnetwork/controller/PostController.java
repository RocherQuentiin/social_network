package com.socialnetwork.socialnetwork.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IMediaService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.FileUpload;
import com.socialnetwork.socialnetwork.dto.PostDto;
import com.socialnetwork.socialnetwork.entity.Media;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.MediaType;
import com.socialnetwork.socialnetwork.enums.VisibilityType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PostController {

    private final IPostRepository postRepository;
    private final IUserService userService;
    private final IMediaService mediaService;
    
    @Autowired
    private UserController userController;

    public PostController(IPostRepository postRepository, IUserService userService, IMediaService mediaService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.mediaService = mediaService;
    }
    
    @PostMapping("/post")
	public String handleCreatePost(Model model, HttpServletRequest request, @RequestParam("content") String content, @RequestParam("postVideoUrl") MultipartFile postVideoUrl, @RequestParam("postImageUrl") MultipartFile postImageUrl, @RequestParam("postFileUrl") MultipartFile postFileUrl, @RequestParam(value = "visibilityType", required = false) String visibilityTypeStr, @RequestParam(value = "allowComments", required = false) String[] allowCommentsValues) {
		System.out.println("post" + postFileUrl);
    	HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			return "login";
		}
		try {
			UUID userId = UUID.fromString(session.getAttribute("userId").toString());
			ResponseEntity<User> author = userService.getUserById(userId);
			
			if(author.getStatusCode() != HttpStatusCode.valueOf(200)) {
				throw new IllegalArgumentException("User not found");
			}
			
			Post post = new Post();
			post.setAuthor(author.getBody());
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
			Post savePost =  postRepository.save(post);
			
			if(postVideoUrl != null && !postVideoUrl.isEmpty()) {
				System.out.println("post Video : " + postVideoUrl);
				String uploadVideoUrl = FileUpload.UploadFile(postVideoUrl);
				long videoSize = postVideoUrl.getSize();
				String extensionVideo = postVideoUrl.getContentType();
				
				Media videoMedia = new Media();
				videoMedia.setFileSize(videoSize);
				videoMedia.setFileUrl(uploadVideoUrl);
				videoMedia.setMediaType(MediaType.VIDEO);
				videoMedia.setMimeType(extensionVideo);
				videoMedia.setPost(savePost);
				videoMedia.setUser(author.getBody());
				
				this.mediaService.create(videoMedia);
			}
			
			if(postImageUrl != null && !postImageUrl.isEmpty()) {
				System.out.println("postImage picture : " + postImageUrl);
				String uploadImageUrl = FileUpload.UploadFile(postImageUrl);
				long imageSize = postImageUrl.getSize();
				String extensionImage = postImageUrl.getContentType();
				
				Media imageMedia = new Media();
				imageMedia.setFileSize(imageSize);
				imageMedia.setFileUrl(uploadImageUrl);
				imageMedia.setMediaType(MediaType.IMAGE);
				imageMedia.setMimeType(extensionImage);
				imageMedia.setPost(savePost);
				imageMedia.setUser(author.getBody());
				
				this.mediaService.create(imageMedia);
			}
			
			if(postFileUrl != null && !postFileUrl.isEmpty()) {
				System.out.println("postFile  : " + postFileUrl);
				String uploadFileUrl = FileUpload.UploadFile(postFileUrl);
				long fileSize = postFileUrl.getSize();
				String extensionFile = postFileUrl.getContentType();
				
				Media fileMedia = new Media();
				fileMedia.setFileSize(fileSize);
				fileMedia.setFileUrl(uploadFileUrl);
				fileMedia.setMediaType(MediaType.DOCUMENT);
				fileMedia.setMimeType(extensionFile);
				fileMedia.setPost(savePost);
				fileMedia.setUser(author.getBody());
				
				this.mediaService.create(fileMedia);
			}
			
			
			
		} catch (Exception e) {
			return "accueil";
		}
		return this.userController.showFeed(model, request);
	}

    @GetMapping("/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") UUID id) {
        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        PostDto dto = new PostDto();
        dto.setId(p.getId());
        dto.setContent(p.getContent());
        dto.setVisibilityType(p.getVisibilityType());
        dto.setAllowComments(p.getAllowComments());
        dto.setAuthorId(p.getAuthor() != null ? p.getAuthor().getId() : null);
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/post/{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") UUID id, @RequestBody PostDto body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID sessionUserId;
        try {
            sessionUserId = UUID.fromString(session.getAttribute("userId").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid session user");
        }

        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        User author = p.getAuthor();
        if (author == null || !sessionUserId.equals(author.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can edit");
        }

        if (body.getContent() != null) p.setContent(body.getContent());
        if (body.getVisibilityType() != null) p.setVisibilityType(body.getVisibilityType());
        if (body.getAllowComments() != null) p.setAllowComments(body.getAllowComments());

        // set updatedAt to Europe/Paris now
        ZonedDateTime nowParis = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        p.setUpdatedAt(LocalDateTime.of(nowParis.toLocalDate(), nowParis.toLocalTime()));

        postRepository.save(p);

        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/post/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") UUID id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
        }
        UUID sessionUserId;
        try {
            sessionUserId = UUID.fromString(session.getAttribute("userId").toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid session user");
        }

        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        Post p = opt.get();
        User author = p.getAuthor();
        if (author == null || !sessionUserId.equals(author.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can delete");
        }

        // set deletedAt to now (Europe/Paris) and mark visibility to PRIVATE to hide
        ZonedDateTime nowParis = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        p.setDeletedAt(LocalDateTime.of(nowParis.toLocalDate(), nowParis.toLocalTime()));
        p.setVisibilityType(VisibilityType.PRIVATE);

        postRepository.save(p);
        return ResponseEntity.ok().build();
    }
}
