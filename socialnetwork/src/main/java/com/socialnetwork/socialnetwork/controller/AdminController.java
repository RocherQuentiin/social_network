package com.socialnetwork.socialnetwork.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;
import com.socialnetwork.socialnetwork.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final IUserRepository userRepository;

    public AdminController(AdminService adminService, IUserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    private boolean isSessionAdmin(HttpSession session) {
        Object uid = session.getAttribute("userId");
        if (uid == null) return false;
        try {
            java.util.UUID id = java.util.UUID.fromString(uid.toString());
            User u = userRepository.findById(id).orElse(null);
            return u != null && u.getRole() == UserRole.ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, org.springframework.ui.Model model) {
        if (!isSessionAdmin(session)) return "redirect:/login";
        model.addAttribute("isConnect", session.getAttribute("userId"));
        // add avatar if available
        try {
            java.util.UUID id = java.util.UUID.fromString(session.getAttribute("userId").toString());
            User u = userRepository.findById(id).orElse(null);
            if (u != null) model.addAttribute("userAvatar", u.getProfilePictureUrl());
        } catch (Exception e) {
            // ignore
        }
        return "admin/dashboard";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> stats(HttpSession session,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        Optional<LocalDateTime> s = Optional.empty();
        Optional<LocalDateTime> e = Optional.empty();
        Map<String, Object> stats = adminService.getBasicStats(s, e);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/stats/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> messageStats(HttpSession session) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        Map<String, Object> stats = adminService.getMessageStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/stats/timeseries")
    @ResponseBody
    public ResponseEntity<?> timeSeries(HttpSession session, @RequestParam(defaultValue = "30") int days) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getTimeSeries(days));
    }

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> users(HttpSession session,
                                   @RequestParam(required = false) String query,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();

        List<com.socialnetwork.socialnetwork.entity.User> all = userRepository.findAll();
        Stream<com.socialnetwork.socialnetwork.entity.User> stream = all.stream();
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            stream = stream.filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(q))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)));
        }

        List<Map<String, Object>> list = stream.skip((long) page * size).limit(size).map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("isActive", u.getIsActive());
            m.put("suspendedUntil", u.getSuspendedUntil());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", list);
        resp.put("page", page);
        resp.put("size", size);
        resp.put("total", all.size());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/user/{id}/block")
    @ResponseBody
    public ResponseEntity<Void> blockUser(HttpSession session, @PathVariable String id) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        boolean ok = adminService.blockUser(UUID.fromString(id));
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/api/user/{id}/unblock")
    @ResponseBody
    public ResponseEntity<Void> unblockUser(HttpSession session, @PathVariable String id) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        boolean ok = adminService.unblockUser(UUID.fromString(id));
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/api/user/{id}/suspend")
    @ResponseBody
    public ResponseEntity<Void> suspendUser(HttpSession session, @PathVariable String id, @RequestBody Map<String, Object> body) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        Integer days = body.get("days") == null ? 0 : Integer.parseInt(body.get("days").toString());
        LocalDateTime until = LocalDateTime.now().plusDays(days);
        boolean ok = adminService.suspendUser(UUID.fromString(id), until);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/post/{id}")
    @ResponseBody
    public ResponseEntity<Void> deletePost(HttpSession session, @PathVariable String id) {
        if (!isSessionAdmin(session)) return ResponseEntity.status(403).build();
        boolean ok = adminService.deletePost(UUID.fromString(id));
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
