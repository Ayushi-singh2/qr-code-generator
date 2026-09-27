package com.example.qrgenerator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic request used to generate a QR code.
 * "type" selects which QR payload builder is used (TEXT, URL, EMAIL, PHONE, WIFI, VCARD).
 * "content" is used directly for TEXT/URL/PHONE (and as a fallback), while the
 * structured fields below are used to build the payload for EMAIL, WIFI and VCARD.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeRequest {

    @NotBlank(message = "QR type is required")
    private String type;

    // Used directly for TEXT / URL / PHONE types
    private String content;

    // Optional QR image size in pixels; backend validates against an allow-list
    private Integer size;

    // ----- EMAIL fields -----
    private String email;
    private String subject;
    private String message;

    // ----- PHONE field (alternative to content) -----
    private String phoneNumber;

    // ----- WIFI fields -----
    private String ssid;
    private String wifiPassword;
    private String encryption; // WPA, WEP, None
    private Boolean hidden;

    // ----- VCARD fields -----
    private String firstName;
    private String lastName;
    private String organization;
    private String vcardPhone;
    private String vcardEmail;
    private String website;
    private String address;
}
