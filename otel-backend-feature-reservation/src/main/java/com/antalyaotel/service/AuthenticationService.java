package com.antalyaotel.service;

import com.antalyaotel.dto.AuthenticationRequest;
import com.antalyaotel.dto.AuthenticationResponse;
import com.antalyaotel.dto.CustomerDTO;
import com.antalyaotel.model.Customer;
import com.antalyaotel.repository.CustomerRepository;
import com.antalyaotel.security.JwtService;
import com.antalyaotel.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Müşteri kimlik doğrulama işlemi.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", customer.getRole());
        String jwt = jwtService.generateToken(claims, customer.getEmail());
        
        return AuthenticationResponse.builder()
                .token(jwt)
                .customerId(customer.getId())
                .build();
    }

    /**
     * Müşteri kayıt işlemi.
     */
    public AuthenticationResponse registerCustomer(CustomerDTO request) {
        // E-posta kontrolü
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı!");
        }

        // TC Kimlik No kontrolü
        if (customerRepository.findByIdNumber(request.getIdNumber()).isPresent()) {
            throw new RuntimeException("Bu TC Kimlik No zaten kayıtlı!");
        }

        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .idNumber(request.getIdNumber())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(request.getBirthDate())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .role(Role.ROLE_CUSTOMER.name())
                .isActive(true)
                .build();

        customer = customerRepository.save(customer);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", customer.getRole());
        String jwt = jwtService.generateToken(claims, customer.getEmail());

        return AuthenticationResponse.builder()
                .token(jwt)
                .customerId(customer.getId())
                .build();
    }
}