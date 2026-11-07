package com.kulikov.conference_app.auth;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Реализация CurrentUser - id человека берётся из контекста безопасности
 */
@Component
public class SecurityCurrentUser implements CurrentUser {

    /**
     * @return id человека, который вошёл; empty, если запрос без входа
     */
    @Override
    public Optional<String> personId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails user) {
            return Optional.of(user.getUsername()); // в username лежит id человека
        }
        return Optional.empty();
    }
}