package com.example.qrgenerator.controller;

import com.example.qrgenerator.config.CustomUserDetailsService;
import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.dto.QRCodeRequest;
import com.example.qrgenerator.dto.QRCodeResponse;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.exception.ResourceNotFoundException;
import com.example.qrgenerator.service.QRCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QRCodeController.class)
class QRCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QRCodeService qrCodeService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserPrincipal principalFor(long id, String email) {
        User user = User.builder().id(id).fullName("User " + id).email(email).password("hash").build();
        return new UserPrincipal(user);
    }

    @Test
    void generate_authenticated_returns201() throws Exception {
        UserPrincipal principal = principalFor(1L, "user1@example.com");
        QRCodeRequest request = new QRCodeRequest();
        request.setType("TEXT");
        request.setContent("hello");

        when(qrCodeService.generate(eq(1L), any())).thenReturn(QRCodeResponse.builder()
                .id(1L).type("TEXT").content("hello").image("data:image/png;base64,xyz").createdAt(LocalDateTime.now()).build());

        mockMvc.perform(post("/api/qrcodes")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TEXT"));
    }

    @Test
    void generate_unauthenticated_returns401() throws Exception {
        QRCodeRequest request = new QRCodeRequest();
        request.setType("TEXT");
        request.setContent("hello");

        mockMvc.perform(post("/api/qrcodes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHistory_authenticated_returnsOnlyOwnCodes() throws Exception {
        UserPrincipal principal = principalFor(1L, "user1@example.com");
        when(qrCodeService.getHistory(1L)).thenReturn(List.of(
                QRCodeResponse.builder().id(1L).type("TEXT").content("hi").image("data:image/png;base64,xyz").createdAt(LocalDateTime.now()).build()
        ));

        mockMvc.perform(get("/api/qrcodes").with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_qrCodeBelongsToAnotherUser_returns404() throws Exception {
        // User A (id=2) tries to access a QR code owned by User B — the service enforces
        // ownership and reports it as not found, never leaking that it belongs to someone else.
        UserPrincipal principal = principalFor(2L, "userB@example.com");
        when(qrCodeService.getById(2L, 99L)).thenThrow(new ResourceNotFoundException("QR code not found"));

        mockMvc.perform(get("/api/qrcodes/99").with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_qrCodeBelongsToAnotherUser_returns404() throws Exception {
        UserPrincipal principal = principalFor(2L, "userB@example.com");
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("QR code not found"))
                .when(qrCodeService).delete(2L, 99L);

        mockMvc.perform(delete("/api/qrcodes/99")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ownedQrCode_returns200() throws Exception {
        UserPrincipal principal = principalFor(1L, "user1@example.com");

        mockMvc.perform(delete("/api/qrcodes/1")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
