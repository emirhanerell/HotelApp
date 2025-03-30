package com.antalyaotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import com.antalyaotel.service.AdminService;
import com.antalyaotel.dto.AdminRegisterRequest;
import com.antalyaotel.dto.AdminLoginRequest;
import com.antalyaotel.dto.AdminResponse;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8000")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminResponse> register(@RequestBody AdminRegisterRequest request) {
        log.info("Admin kayıt isteği alındı: {}", request.getEmail());
        return ResponseEntity.ok(adminService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        log.info("Admin giriş isteği alındı: {}", request.getEmail());
        try {
            AdminResponse response = adminService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Admin girişi başarısız: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 🔥 Sadece ADMIN erişebilir
    public String dashboard() {
        return "Admin Dashboard";
    }
}


