package com.kulikov.conference_app.auth;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Реализация CurrentUser - id человека берётся из контекста безопасности
 */
@Component
public class SecurityCurrentUser implements CurrentUser {

    @Override
    public Optional<String> personId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof PersonUserDetails details) {
            return Optional.of(details.getPersonId());
        }
        return Optional.empty();
    }
}