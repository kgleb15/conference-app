package com.kulikov.conference_app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ModerationRequest(@NotBlank String comment) {
}
