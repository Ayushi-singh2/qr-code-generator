package com.example.qrgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Simple success-message envelope used by endpoints that don't return a resource. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;

    public static ApiResponse of(boolean success, String message) {
        return new ApiResponse(success, message);
    }
}
