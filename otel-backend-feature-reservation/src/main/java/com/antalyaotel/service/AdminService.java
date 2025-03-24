package com.antalyaotel.service;

import com.antalyaotel.dto.AdminLoginRequest;
import com.antalyaotel.dto.AdminRegisterRequest;
import com.antalyaotel.dto.AdminResponse;
import com.antalyaotel.model.Admin;
import com.antalyaotel.repository.AdminRepository;
import com.antalyaotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AdminResponse register(AdminRegisterRequest request) {
        log.info("Admin kayıt işlemi başlatıldı: {}", request.getEmail());
        
        try {
            // Email kontrolü
            if (adminRepository.existsByEmail(request.getEmail())) {
                log.error("Email adresi zaten kayıtlı: {}", request.getEmail());
                throw new RuntimeException("Bu email adresi zaten kayıtlı");
            }

            // Yeni admin oluştur
            Admin admin = Admin.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            // Admin'i kaydet
            admin = adminRepository.save(admin);
            log.info("Admin başarıyla kaydedildi: {}", admin.getEmail());

            // JWT token oluştur
            String token = jwtService.generateToken(admin);
            log.info("JWT token oluşturuldu");

            // Response döndür
            return AdminResponse.builder()
                    .id(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .token(token)
                    .build();
        } catch (Exception e) {
            log.error("Admin kayıt işlemi başarısız: {}", e.getMessage());
            throw new RuntimeException("Admin kayıt işlemi başarısız: " + e.getMessage());
        }
    }

    public AdminResponse login(AdminLoginRequest request) {
        log.info("Admin giriş işlemi başlatıldı: {}", request.getEmail());
        
        try {
            // Kimlik doğrulama
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Admin'i bul
            Admin admin = adminRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("Admin bulunamadı: {}", request.getEmail());
                        return new RuntimeException("Admin bulunamadı");
                    });

            // JWT token oluştur
            String token = jwtService.generateToken(admin);
            log.info("Admin başarıyla giriş yaptı: {}", admin.getEmail());

            // Response döndür
            return AdminResponse.builder()
                    .id(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .token(token)
                    .build();
        } catch (Exception e) {
            log.error("Admin giriş işlemi başarısız: {}", e.getMessage());
            throw new RuntimeException("Admin giriş işlemi başarısız: " + e.getMessage());
        }
    }
} 