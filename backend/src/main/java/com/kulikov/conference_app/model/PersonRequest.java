package com.kulikov.conference_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PersonRequest(
        @NotBlank String fullName,
        @NotBlank String groupName,
        @NotBlank @Email String email,
        @NotBlank String phone) {
}
