package com.example.qrgenerator.controller;

import com.example.qrgenerator.config.CustomUserDetailsService;
import com.example.qrgenerator.config.SecurityConfig;
import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.dto.LoginRequest;
import com.example.qrgenerator.dto.RegisterRequest;
import com.example.qrgenerator.dto.UserResponse;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Required by SecurityConfig's DaoAuthenticationProvider bean even though this slice doesn't hit it directly
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", "password123");
        when(authService.register(any())).thenReturn(UserResponse.builder()
                .id(1L).fullName("Jane Doe").email("jane@example.com").createdAt(LocalDateTime.now()).build());

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "not-an-email", "password123", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "short", "short");

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returnsUser() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        when(authService.login(any(), any(), any())).thenReturn(UserResponse.builder()
                .id(1L).fullName("Jane Doe").email("jane@example.com").createdAt(LocalDateTime.now()).build());

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                // Never expose the password hash
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void me_authenticatedUser_returnsCurrentUser() throws Exception {
        User user = User.builder().id(1L).fullName("Jane Doe").email("jane@example.com").password("hashedvalue").build();
        UserPrincipal principal = new UserPrincipal(user);
        when(authService.getCurrentUser(any())).thenReturn(UserResponse.builder()
                .id(1L).fullName("Jane Doe").email("jane@example.com").createdAt(LocalDateTime.now()).build());

        mockMvc.perform(get("/api/auth/me").with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}