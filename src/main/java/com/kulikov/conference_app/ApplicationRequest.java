package com.kulikov.conference_app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record ApplicationRequest(
        @NotBlank String title,
        @NotBlank String thesisText,
        @NotBlank String authorFullName,
        @NotBlank String authorGroup,
        @Email String authorEmail,
        @NotBlank String authorPhone) {
}
