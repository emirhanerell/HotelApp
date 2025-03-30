package com.antalyaotel.dto;

import lombok.Data;

@Data
public class AdminRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role = "ROLE_ADMIN"; // Varsayılan olarak ROLE_ADMIN
} 