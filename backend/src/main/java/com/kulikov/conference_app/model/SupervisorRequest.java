package com.kulikov.conference_app.model;

import jakarta.validation.constraints.NotBlank;

public record SupervisorRequest(@NotBlank String fullName, @NotBlank String workplace) {
}
