package com.example.qrgenerator.controller;

import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.dto.ApiResponse;
import com.example.qrgenerator.dto.QRCodeRequest;
import com.example.qrgenerator.dto.QRCodeResponse;
import com.example.qrgenerator.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @PostMapping
    public ResponseEntity<QRCodeResponse> generate(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody QRCodeRequest request) {
        QRCodeResponse response = qrCodeService.generate(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QRCodeResponse>> getHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(qrCodeService.getHistory(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRCodeResponse> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(qrCodeService.getById(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id) {
        qrCodeService.delete(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.of(true, "QR code deleted successfully"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long id) {
        byte[] pngBytes = qrCodeService.getDownloadBytes(principal.getId(), id);
        String fileName = qrCodeService.getFileName(principal.getId(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(pngBytes);
    }
}
