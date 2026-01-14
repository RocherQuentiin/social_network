package com.socialnetwork.socialnetwork.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;
import com.socialnetwork.socialnetwork.service.AdminService;
import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private IUserRepository userRepository;

    @Test
    public void stats_whenNoSession_thenForbidden() throws Exception {
        mockMvc.perform(get("/admin/api/stats")).andExpect(status().isForbidden());
    }

    @Test
    public void stats_whenAdminSession_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        User admin = new User();
        admin.setId(id);
        admin.setRole(UserRole.ADMIN);
        when(userRepository.findById(id)).thenReturn(Optional.of(admin));
        Map<String,Object> stats = new HashMap<>();
        stats.put("totalUsers", 1);
        when(adminService.getBasicStats(Optional.empty(), Optional.empty())).thenReturn(stats);

        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/admin/api/stats");
        req.sessionAttr("userId", id.toString());

        mockMvc.perform(req).andExpect(status().isOk());
    }
}
