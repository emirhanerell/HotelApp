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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AdminResponse register(AdminRegisterRequest request) {
        log.info("Admin kaydı başlatılıyor: {}", request.getEmail());
        
        try {
            // Email kontrolü
            if (adminRepository.existsByEmail(request.getEmail())) {
                log.error("Bu email adresi zaten kullanımda: {}", request.getEmail());
                throw new RuntimeException("Bu email adresi zaten kullanımda");
            }

            // Yeni admin oluştur
            Admin admin = Admin.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role("ROLE_ADMIN")
                    .build();

            // Admin'i kaydet
            Admin savedAdmin = adminRepository.save(admin);
            log.info("Admin başarıyla kaydedildi: {}", savedAdmin.getEmail());

            // JWT token oluştur
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ROLE_ADMIN");
            String token = jwtService.generateToken(claims, savedAdmin.getEmail());
            log.info("JWT token oluşturuldu");

            return AdminResponse.builder()
                    .token(token)
                    .build();
        } catch (Exception e) {
            log.error("Admin kaydı başarısız: {}", e.getMessage());
            throw new RuntimeException("Admin kaydı başarısız: " + e.getMessage());
        }
    }

    public AdminResponse login(AdminLoginRequest request) {
        log.info("Admin girişi başlatılıyor: {}", request.getEmail());
        
        try {
            // Admin'i bul
            Admin admin = adminRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("Admin bulunamadı: {}", request.getEmail());
                        return new RuntimeException("Admin bulunamadı");
                    });
            log.info("Admin bulundu: {}", admin.getEmail());

            // Kimlik doğrulama
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.info("Kimlik doğrulama başarılı");

            // JWT token oluştur
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", admin.getRole());
            String token = jwtService.generateToken(claims, admin.getEmail());
            log.info("JWT token oluşturuldu");

            return AdminResponse.builder()
                    .token(token)
                    .build();
        } catch (Exception e) {
            log.error("Admin girişi başarısız: {}", e.getMessage());
            throw new RuntimeException("Giriş başarısız: " + e.getMessage());
        }
    }
} 