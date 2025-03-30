package com.antalyaotel.service;

import com.antalyaotel.model.Admin;
import com.antalyaotel.model.Customer;
import com.antalyaotel.repository.AdminRepository;
import com.antalyaotel.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Önce admin'de ara
        return adminRepository.findByEmail(username)
                .map(this::createUserDetails)
                .orElseGet(() -> {
                    // Admin'de bulunamazsa customer'da ara
                    return customerRepository.findByEmail(username)
                            .map(this::createUserDetails)
                            .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
                });
    }

    private UserDetails createUserDetails(Admin admin) {
        return new User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(admin.getRole()))
        );
    }

    private UserDetails createUserDetails(Customer customer) {
        return new User(
                customer.getEmail(),
                customer.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(customer.getRole()))
        );
    }
}