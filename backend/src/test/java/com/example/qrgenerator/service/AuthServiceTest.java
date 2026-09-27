package com.example.qrgenerator.service;

import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.dto.LoginRequest;
import com.example.qrgenerator.dto.RegisterRequest;
import com.example.qrgenerator.dto.UserResponse;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.exception.BadRequestException;
import com.example.qrgenerator.exception.DuplicateEmailException;
import com.example.qrgenerator.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, securityContextRepository);
    }

    @Test
    void register_successfulRegistration_savesHashedPassword() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", "password123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getFullName()).isEqualTo("Jane Doe");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        // Password hashing: the stored value must never equal the raw password, and must verify via BCrypt
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void register_duplicateEmail_throwsDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", "password123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordMismatch_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123", "differentPassword");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsUserResponseAndPersistsSecurityContext() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        User user = User.builder()
                .id(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password(passwordEncoder.encode("password123"))
                .createdAt(LocalDateTime.now())
                .build();
        UserPrincipal principal = new UserPrincipal(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.login(request, httpRequest, httpResponse);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
    }

    @Test
    void login_invalidPassword_propagatesBadCredentialsException() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrongPassword");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, httpRequest, httpResponse))
                .isInstanceOf(BadCredentialsException.class);
    }
}
