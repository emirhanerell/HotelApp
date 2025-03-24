package com.antalyaotel.controller;

import com.antalyaotel.dto.AuthenticationRequest;
import com.antalyaotel.dto.AuthenticationResponse;
import com.antalyaotel.dto.CustomerDTO;
import com.antalyaotel.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody CustomerDTO request) {
        AuthenticationResponse response = authenticationService.registerCustomer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<?> testAuthentication(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(403).body("No authentication found");
        }
        return ResponseEntity.ok("User: " + authentication.getName() + ", Roles: " + authentication.getAuthorities());
    }
}
