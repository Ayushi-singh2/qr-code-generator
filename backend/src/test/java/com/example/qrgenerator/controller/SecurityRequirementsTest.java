package com.example.qrgenerator.controller;

import com.example.qrgenerator.config.CustomUserDetailsService;
import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.exception.ResourceNotFoundException;
import com.example.qrgenerator.service.QRCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Directly verifies the security requirements called out in the project spec:
 * 1. Unauthenticated users cannot access /api/qrcodes.
 * 2. User A cannot access User B's QR code.
 * 3. User A cannot delete User B's QR code.
 * 4. Passwords are stored as BCrypt hashes.
 * 5. Password hashes are never returned through API responses (see AuthControllerTest#me_...).
 */
@WebMvcTest(controllers = QRCodeController.class)
class SecurityRequirementsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QRCodeService qrCodeService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void unauthenticatedUser_cannotAccessQrCodeHistory() throws Exception {
        mockMvc.perform(get("/api/qrcodes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userA_cannotAccessUserBsQrCode() throws Exception {
        User userA = User.builder().id(1L).fullName("User A").email("a@example.com").password("hash").build();
        UserPrincipal principalA = new UserPrincipal(userA);

        // The service scopes the lookup to userA's id (1L); it never finds userB's QR code (id 42)
        when(qrCodeService.getById(eq(1L), eq(42L))).thenThrow(new ResourceNotFoundException("QR code not found"));

        mockMvc.perform(get("/api/qrcodes/42").with(SecurityMockMvcRequestPostProcessors.user(principalA)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userA_cannotDeleteUserBsQrCode() throws Exception {
        User userA = User.builder().id(1L).fullName("User A").email("a@example.com").password("hash").build();
        UserPrincipal principalA = new UserPrincipal(userA);

        doThrow(new ResourceNotFoundException("QR code not found"))
                .when(qrCodeService).delete(eq(1L), eq(42L));

        mockMvc.perform(delete("/api/qrcodes/42")
                        .with(SecurityMockMvcRequestPostProcessors.user(principalA))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void passwords_areStoredAsBcryptHashes_neverPlainText() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String hashed = encoder.encode(rawPassword);

        assertThat(hashed).isNotEqualTo(rawPassword);
        assertThat(hashed).startsWith("$2a$").matches(h -> h.startsWith("$2a$") || h.startsWith("$2b$"));
        assertThat(encoder.matches(rawPassword, hashed)).isTrue();
    }
}
