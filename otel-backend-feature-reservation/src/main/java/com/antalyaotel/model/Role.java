package com.antalyaotel.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_CUSTOMER;

    @Override
    public String getAuthority() {
        return name(); // ✅ Spring Security'nin beklentisini karşılar
    }
}
