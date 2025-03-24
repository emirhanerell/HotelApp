package com.antalyaotel.service;

import com.antalyaotel.model.Role;
import com.antalyaotel.model.User;
import com.antalyaotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new CustomUserDetails(user); // CustomUserDetails sınıfı, User sınıfını UserDetails'e uyarlayacak
    }

    // CustomUserDetails sınıfı, UserDetails'i implement eder
    private static class CustomUserDetails implements UserDetails {

        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())); // "ROLE_" eklenerek rol döndürülür
        }

        @Override
        public String getPassword() {
            return user.getPassword(); // Kullanıcı şifresi
        }

        @Override
        public String getUsername() {
            return user.getEmail(); // Kullanıcı adı (bu örnekte e-posta kullanılıyor)
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // Hesap süresi dolmuş mu kontrolü
        }

        @Override
        public boolean isAccountNonLocked() {
            return true; // Hesap kilitli mi kontrolü
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Kimlik bilgileri süresi dolmuş mu kontrolü
        }

        @Override
        public boolean isEnabled() {
            return true; // Hesap aktif mi kontrolü
        }
    }
}



