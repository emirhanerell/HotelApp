package com.antalyaotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 🔥 Sadece ADMIN erişebilir
    public String dashboard() {
        return "Admin Dashboard";
    }
}


