package com.example.qrgenerator.service;

import com.example.qrgenerator.dto.QRCodeRequest;
import com.example.qrgenerator.dto.QRCodeResponse;
import com.example.qrgenerator.entity.QRCode;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.exception.ResourceNotFoundException;
import com.example.qrgenerator.repository.QRCodeRepository;
import com.example.qrgenerator.repository.UserRepository;
import com.example.qrgenerator.util.QRCodeGenerator;
import com.example.qrgenerator.util.QRContentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private UserRepository userRepository;

    private QRCodeGenerator qrCodeGenerator;
    private QRContentBuilder qrContentBuilder;
    private QRCodeService qrCodeService;

    private User owner;

    @BeforeEach
    void setUp() {
        qrCodeGenerator = new QRCodeGenerator();
        qrContentBuilder = new QRContentBuilder();
        qrCodeService = new QRCodeService(qrCodeRepository, userRepository, qrCodeGenerator, qrContentBuilder);

        owner = User.builder().id(1L).fullName("Owner").email("owner@example.com").password("hash").build();
    }

    private QRCodeRequest requestOf(String type) {
        QRCodeRequest req = new QRCodeRequest();
        req.setType(type);
        return req;
    }

    @Test
    void generate_urlType_producesQrCodeAndSavesRecord() {
        QRCodeRequest req = requestOf("URL");
        req.setContent("https://example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(inv -> {
            QRCode qr = inv.getArgument(0);
            qr.setId(10L);
            qr.setCreatedAt(LocalDateTime.now());
            return qr;
        });

        QRCodeResponse response = qrCodeService.generate(1L, req);

        assertThat(response.getType()).isEqualTo("URL");
        assertThat(response.getContent()).isEqualTo("https://example.com");
        assertThat(response.getImage()).startsWith("data:image/png;base64,");
    }

    @Test
    void generate_textType_producesQrCode() {
        QRCodeRequest req = requestOf("TEXT");
        req.setContent("Hello World");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(inv -> {
            QRCode qr = inv.getArgument(0);
            qr.setId(11L);
            qr.setCreatedAt(LocalDateTime.now());
            return qr;
        });

        QRCodeResponse response = qrCodeService.generate(1L, req);

        assertThat(response.getContent()).isEqualTo("Hello World");
    }

    @Test
    void generate_emailType_buildsMailtoContent() {
        QRCodeRequest req = requestOf("EMAIL");
        req.setEmail("test@example.com");
        req.setSubject("Hi");
        req.setMessage("Hello there");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(inv -> {
            QRCode qr = inv.getArgument(0);
            qr.setId(12L);
            qr.setCreatedAt(LocalDateTime.now());
            return qr;
        });

        QRCodeResponse response = qrCodeService.generate(1L, req);

        assertThat(response.getContent()).startsWith("mailto:test@example.com?subject=");
    }

    @Test
    void generate_wifiType_buildsWifiContent() {
        QRCodeRequest req = requestOf("WIFI");
        req.setSsid("MyNetwork");
        req.setWifiPassword("secret123");
        req.setEncryption("WPA");
        req.setHidden(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(inv -> {
            QRCode qr = inv.getArgument(0);
            qr.setId(13L);
            qr.setCreatedAt(LocalDateTime.now());
            return qr;
        });

        QRCodeResponse response = qrCodeService.generate(1L, req);

        assertThat(response.getContent()).startsWith("WIFI:T:WPA;S:MyNetwork;P:secret123;");
    }

    @Test
    void generate_vcardType_buildsVCardContent() {
        QRCodeRequest req = requestOf("VCARD");
        req.setFirstName("John");
        req.setLastName("Smith");
        req.setVcardPhone("+1234567890");
        req.setVcardEmail("john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(qrCodeRepository.save(any(QRCode.class))).thenAnswer(inv -> {
            QRCode qr = inv.getArgument(0);
            qr.setId(14L);
            qr.setCreatedAt(LocalDateTime.now());
            return qr;
        });

        QRCodeResponse response = qrCodeService.generate(1L, req);

        assertThat(response.getContent()).contains("BEGIN:VCARD");
        assertThat(response.getContent()).contains("FN:John Smith");
    }

    @Test
    void getHistory_returnsOnlyRequestingUsersQrCodes() {
        QRCode qr1 = QRCode.builder().id(1L).user(owner).qrType("TEXT").content("hello").createdAt(LocalDateTime.now()).build();
        when(qrCodeRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(qr1));

        List<QRCodeResponse> history = qrCodeService.getHistory(1L);

        assertThat(history).hasSize(1);
        verify(qrCodeRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void delete_ownedQrCode_deletesSuccessfully() {
        QRCode qr = QRCode.builder().id(5L).user(owner).qrType("TEXT").content("hi").build();
        when(qrCodeRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(qr));

        qrCodeService.delete(1L, 5L);

        verify(qrCodeRepository).delete(qr);
    }

    @Test
    void delete_unauthorizedAccess_qrCodeBelongsToAnotherUser_throwsNotFound() {
        // findByIdAndUserId is scoped to the requesting user, so another user's QR code looks absent
        when(qrCodeRepository.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrCodeService.delete(2L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(qrCodeRepository, never()).delete(any());
    }

    @Test
    void getById_unauthorizedAccess_throwsResourceNotFound() {
        when(qrCodeRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrCodeService.getById(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
