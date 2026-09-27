package com.example.qrgenerator.util;

import com.example.qrgenerator.dto.QRCodeRequest;
import com.example.qrgenerator.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds the actual QR-encodable string for each supported QR type
 * from the structured fields on a QRCodeRequest.
 */
@Component
public class QRContentBuilder {

    public enum QRType {
        TEXT, URL, EMAIL, PHONE, WIFI, VCARD
    }

    public QRType parseType(String rawType) {
        if (rawType == null) {
            throw new BadRequestException("QR type is required");
        }
        try {
            return QRType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid QR code type: " + rawType);
        }
    }

    public String build(QRType type, QRCodeRequest req) {
        return switch (type) {
            case TEXT -> buildText(req);
            case URL -> buildUrl(req);
            case EMAIL -> buildEmail(req);
            case PHONE -> buildPhone(req);
            case WIFI -> buildWifi(req);
            case VCARD -> buildVCard(req);
        };
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value.trim();
    }

    private String buildText(QRCodeRequest req) {
        return requireNonBlank(req.getContent(), "Text content");
    }

    private String buildUrl(QRCodeRequest req) {
        String url = requireNonBlank(req.getContent(), "URL");
        if (!url.matches("^(https?|ftp)://.*$")) {
            url = "https://" + url;
        }
        return url;
    }

    private String buildPhone(QRCodeRequest req) {
        String phone = req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                ? req.getPhoneNumber() : req.getContent();
        phone = requireNonBlank(phone, "Phone number");
        return "tel:" + phone.replaceAll("\\s+", "");
    }

    private String buildEmail(QRCodeRequest req) {
        String email = requireNonBlank(req.getEmail(), "Email");
        String subject = req.getSubject() != null ? req.getSubject() : "";
        String message = req.getMessage() != null ? req.getMessage() : "";
        String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        return "mailto:" + email + "?subject=" + encodedSubject + "&body=" + encodedMessage;
    }

    private String buildWifi(QRCodeRequest req) {
        String ssid = requireNonBlank(req.getSsid(), "SSID");
        String password = req.getWifiPassword() != null ? req.getWifiPassword() : "";
        String encryption = req.getEncryption() != null ? req.getEncryption().trim().toUpperCase() : "WPA";
        if (!encryption.equals("WPA") && !encryption.equals("WEP") && !encryption.equals("NONE")) {
            throw new BadRequestException("Encryption must be one of WPA, WEP, None");
        }
        String zxingEncryption = encryption.equals("NONE") ? "nopass" : encryption;
        boolean hidden = Boolean.TRUE.equals(req.getHidden());

        StringBuilder sb = new StringBuilder("WIFI:");
        sb.append("T:").append(zxingEncryption).append(';');
        sb.append("S:").append(escapeWifi(ssid)).append(';');
        if (!zxingEncryption.equals("nopass")) {
            sb.append("P:").append(escapeWifi(password)).append(';');
        }
        sb.append("H:").append(hidden).append(';');
        sb.append(';');
        return sb.toString();
    }

    private String escapeWifi(String value) {
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:");
    }

    private String buildVCard(QRCodeRequest req) {
        String firstName = req.getFirstName() != null ? req.getFirstName().trim() : "";
        String lastName = req.getLastName() != null ? req.getLastName().trim() : "";
        if (firstName.isBlank() && lastName.isBlank()) {
            throw new BadRequestException("First name or last name is required");
        }
        String org = nullToEmpty(req.getOrganization());
        String phone = nullToEmpty(req.getVcardPhone());
        String email = nullToEmpty(req.getVcardEmail());
        String website = nullToEmpty(req.getWebsite());
        String address = nullToEmpty(req.getAddress());

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        sb.append("N:").append(lastName).append(';').append(firstName).append('\n');
        sb.append("FN:").append((firstName + " " + lastName).trim()).append('\n');
        if (!org.isBlank()) sb.append("ORG:").append(org).append('\n');
        if (!phone.isBlank()) sb.append("TEL:").append(phone).append('\n');
        if (!email.isBlank()) sb.append("EMAIL:").append(email).append('\n');
        if (!website.isBlank()) sb.append("URL:").append(website).append('\n');
        if (!address.isBlank()) sb.append("ADR:;;").append(address).append(";;;;").append('\n');
        sb.append("END:VCARD");
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
