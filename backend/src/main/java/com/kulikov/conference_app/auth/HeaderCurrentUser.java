package com.kulikov.conference_app.auth;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Реализация CurrentUser: id участника берётся из заголовка
 */
@Component
public class HeaderCurrentUser implements CurrentUser {

    private static final String HEADER = "Person-Id";

    private final HttpServletRequest _request; // Запрос

    public HeaderCurrentUser(HttpServletRequest request) {
        this._request = request;
    }

    @Override
    public Optional<String> personId() {
        String id = _request.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(id);
    }
}
