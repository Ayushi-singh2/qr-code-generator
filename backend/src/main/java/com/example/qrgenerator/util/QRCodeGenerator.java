package com.example.qrgenerator.util;

import com.example.qrgenerator.exception.BadRequestException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Wraps ZXing to turn arbitrary text content into a PNG QR code.
 * Dimensions are validated against a fixed allow-list — the frontend's
 * requested size is never trusted blindly.
 */
@Component
public class QRCodeGenerator {

    public static final int DEFAULT_SIZE = 300;
    private static final Set<Integer> ALLOWED_SIZES = Set.of(200, 300, 500);

    /**
     * Generates a QR code PNG for the given content at the given width/height.
     */
    public byte[] generateQRCode(String content, int width, int height) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("QR code content must not be empty");
        }
        if (content.length() > 4000) {
            throw new BadRequestException("QR code content is too long");
        }

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new BadRequestException("Failed to generate QR code: " + e.getMessage());
        }
    }

    /** Generates a QR code at the default 300x300 size. */
    public byte[] generateQRCode(String content) {
        return generateQRCode(content, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /** Encodes PNG bytes as a data URL usable directly as an <img> src in the frontend. */
    public String toBase64DataUrl(byte[] pngBytes) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes);
    }

    /**
     * Validates a client-requested size against a fixed allow-list, falling back
     * to the default when null. Never trust a raw client-supplied dimension.
     */
    public int resolveValidSize(Integer requestedSize) {
        if (requestedSize == null) {
            return DEFAULT_SIZE;
        }
        if (!ALLOWED_SIZES.contains(requestedSize)) {
            throw new BadRequestException("Invalid QR code size. Allowed sizes: " + ALLOWED_SIZES);
        }
        return requestedSize;
    }
}
