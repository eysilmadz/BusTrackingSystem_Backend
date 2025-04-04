package com.RotaDurak.RotaDurak.dto;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String email;
}

