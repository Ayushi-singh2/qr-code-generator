package com.example.qrgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeResponse {
    private Long id;
    private String type;
    private String content;
    private String image; // data:image/png;base64,...
    private LocalDateTime createdAt;
}
