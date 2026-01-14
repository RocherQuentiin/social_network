package com.socialnetwork.socialnetwork.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IPostRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IMessageRepository;
import com.socialnetwork.socialnetwork.entity.Post;
import com.socialnetwork.socialnetwork.entity.User;

@Service
public class AdminService {

    private final IUserRepository userRepository;
    private final IPostRepository postRepository;
    private final IMessageRepository messageRepository;
    private final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public AdminService(IUserRepository userRepository, IPostRepository postRepository, IMessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.messageRepository = messageRepository;
    }

    public Map<String, Object> getBasicStats(Optional<LocalDateTime> start, Optional<LocalDateTime> end) {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("activeUsers", activeUsers);
        // Additional fields can be added (period filters) later
        return stats;
    }

    public Map<String, Object> getMessageStats() {
        Map<String, Object> m = new HashMap<>();
        long totalMessages = messageRepository.count();
        long totalUsers = userRepository.count();
        double avgPerUser = totalUsers > 0 ? (double) totalMessages / totalUsers : 0.0;
        m.put("totalMessages", totalMessages);
        m.put("avgMessagesPerUser", avgPerUser);
        return m;
    }

    public Map<String, List<Map<String, Object>>> getTimeSeries(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(days);
        Map<String, Integer> usersPerDay = new HashMap<>();
        Map<String, Integer> postsPerDay = new HashMap<>();

        // initialize days
        for (int i = 0; i <= days; i++) {
            LocalDateTime d = from.plusDays(i);
            String key = d.toLocalDate().toString();
            usersPerDay.put(key, 0);
            postsPerDay.put(key, 0);
        }

        // aggregate users
        userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(from))
                .forEach(u -> {
                    String k = u.getCreatedAt().toLocalDate().toString();
                    usersPerDay.computeIfPresent(k, (kk, v) -> v + 1);
                });

        // aggregate posts
        postRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null && !p.getCreatedAt().isBefore(from))
                .forEach(p -> {
                    String k = p.getCreatedAt().toLocalDate().toString();
                    postsPerDay.computeIfPresent(k, (kk, v) -> v + 1);
                });

        List<Map<String, Object>> usersSeries = usersPerDay.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> {
            Map<String, Object> m2 = new HashMap<>(); m2.put("date", e.getKey()); m2.put("count", e.getValue()); return m2;
        }).toList();

        List<Map<String, Object>> postsSeries = postsPerDay.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> {
            Map<String, Object> m2 = new HashMap<>(); m2.put("date", e.getKey()); m2.put("count", e.getValue()); return m2;
        }).toList();

        Map<String, List<Map<String, Object>>> out = new HashMap<>();
        out.put("users", usersSeries);
        out.put("posts", postsSeries);
        return out;
    }

    public boolean blockUser(UUID userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            u.setIsActive(false);
            userRepository.save(u);
            logger.info("Admin blocked user {}", userId);
            return true;
        }
        return false;
    }

    public boolean unblockUser(UUID userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            u.setIsActive(true);
            userRepository.save(u);
            logger.info("Admin unblocked user {}", userId);
            return true;
        }
        return false;
    }

    public boolean suspendUser(UUID userId, LocalDateTime until) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            u.setSuspendedUntil(until);
            userRepository.save(u);
            logger.info("Admin suspended user {} until {}", userId, until);
            return true;
        }
        return false;
    }

    public boolean deletePost(UUID postId) {
        Optional<Post> opt = postRepository.findById(postId);
        if (opt.isPresent()) {
            Post p = opt.get();
            p.setDeletedAt(LocalDateTime.now());
            postRepository.save(p);
            logger.info("Admin deleted post {}", postId);
            return true;
        }
        return false;
    }
}
