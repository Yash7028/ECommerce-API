package com.ecommerce.ecomapi.utility;

import com.ecommerce.ecomapi.config.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SecurityUtil{


    public CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        throw new IllegalStateException("User is not authenticated");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail(){
        return getCurrentUser().getUsername();
    }
    public Collection<? extends GrantedAuthority> getCurrentUserAuthorities(){
        return getCurrentUser().getAuthorities();
    }

}
