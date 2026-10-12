package com.kulikov.conference_app.model;

import jakarta.validation.constraints.NotBlank;

public record ApplicationRequest(
        @NotBlank String title,
        @NotBlank String thesisText) {
}
