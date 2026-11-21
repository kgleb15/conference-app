package com.kulikov.conference_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Данные организатора или члена экспертной комиссии
 */
public record StaffRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Integer directionId) {

    @Override
    public String toString() {
        return "StaffRequest[fullName=" + fullName + ", email=" + email
                + ", directionId=" + directionId + ", password=***]";
    }
}
