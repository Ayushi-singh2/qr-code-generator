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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final UserRepository userRepository;
    private final QRCodeGenerator qrCodeGenerator;
    private final QRContentBuilder qrContentBuilder;

    @Transactional
    public QRCodeResponse generate(Long userId, QRCodeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        QRContentBuilder.QRType type = qrContentBuilder.parseType(request.getType());
        String content = qrContentBuilder.build(type, request);
        int size = qrCodeGenerator.resolveValidSize(request.getSize());

        byte[] pngBytes = qrCodeGenerator.generateQRCode(content, size, size);
        String dataUrl = qrCodeGenerator.toBase64DataUrl(pngBytes);

        QRCode qrCode = QRCode.builder()
                .user(user)
                .qrType(type.name())
                .content(content)
                .fileName("qr_" + type.name().toLowerCase() + "_" + System.currentTimeMillis() + ".png")
                .build();
        QRCode saved = qrCodeRepository.save(qrCode);

        return QRCodeResponse.builder()
                .id(saved.getId())
                .type(saved.getQrType())
                .content(saved.getContent())
                .image(dataUrl)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<QRCodeResponse> getHistory(Long userId) {
        return qrCodeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(qr -> QRCodeResponse.builder()
                        .id(qr.getId())
                        .type(qr.getQrType())
                        .content(qr.getContent())
                        .image(qrCodeGenerator.toBase64DataUrl(qrCodeGenerator.generateQRCode(qr.getContent())))
                        .createdAt(qr.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public QRCodeResponse getById(Long userId, Long qrCodeId) {
        QRCode qrCode = getOwnedQrCodeOrThrow(userId, qrCodeId);
        byte[] pngBytes = qrCodeGenerator.generateQRCode(qrCode.getContent());
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .type(qrCode.getQrType())
                .content(qrCode.getContent())
                .image(qrCodeGenerator.toBase64DataUrl(pngBytes))
                .createdAt(qrCode.getCreatedAt())
                .build();
    }

    @Transactional
    public void delete(Long userId, Long qrCodeId) {
        QRCode qrCode = getOwnedQrCodeOrThrow(userId, qrCodeId);
        qrCodeRepository.delete(qrCode);
    }

    /** Regenerates the PNG bytes for download, after verifying the requester owns this QR code. */
    @Transactional(readOnly = true)
    public byte[] getDownloadBytes(Long userId, Long qrCodeId) {
        QRCode qrCode = getOwnedQrCodeOrThrow(userId, qrCodeId);
        return qrCodeGenerator.generateQRCode(qrCode.getContent());
    }

    @Transactional(readOnly = true)
    public String getFileName(Long userId, Long qrCodeId) {
        QRCode qrCode = getOwnedQrCodeOrThrow(userId, qrCodeId);
        return qrCode.getFileName() != null ? qrCode.getFileName() : "qrcode.png";
    }

    /**
     * Looks up a QR code scoped to the requesting user's id — never trusts a bare id lookup.
     * A QR code belonging to another user simply doesn't exist from this user's perspective.
     */
    private QRCode getOwnedQrCodeOrThrow(Long userId, Long qrCodeId) {
        return qrCodeRepository.findByIdAndUserId(qrCodeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("QR code not found"));
    }
}
