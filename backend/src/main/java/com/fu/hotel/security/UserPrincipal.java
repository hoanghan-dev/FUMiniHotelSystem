package com.fu.hotel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final String email;
    private final String password;
    private final String role; // "ROLE_STAFF" or "ROLE_CUSTOMER"
    private final Integer customerId;
    private final String fullName;
    private final boolean active;

    public UserPrincipal(String email, String password, String role,
                         Integer customerId, String fullName, boolean active) {
        this.email = email;
        this.password = password;
        this.role = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        this.customerId = customerId;
        this.fullName = fullName;
        this.active = active;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
