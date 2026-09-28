package com.kulikov.conference_app;

public record ApplicationRequest(
        String title,
        String thesisText,
        String authorFullName,
        String authorGroup,
        String authorEmail,
        String authorPhone) {
}
