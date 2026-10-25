package com.kulikov.conference_app.model;

import jakarta.validation.constraints.NotBlank;

public record ModerationRequest(@NotBlank String comment) {
}
