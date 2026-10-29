package com.metro.authservice.dto.response;

import lombok.Data;

@Data
public class RegisterResponseDto {
    private Long id;
    private String email;
    private String name;
    private String message;
}
