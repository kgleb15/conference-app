package com.kulikov.conference_app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SupervisorRequest(@NotBlank String fullName, @NotBlank String workplace) {
}
