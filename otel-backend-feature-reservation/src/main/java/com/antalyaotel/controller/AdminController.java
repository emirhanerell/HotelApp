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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminResponse> register(@RequestBody AdminRegisterRequest request) {
        return ResponseEntity.ok(adminService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 🔥 Sadece ADMIN erişebilir
    public String dashboard() {
        return "Admin Dashboard";
    }
}


