package com.kulikov.conference_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonRequest(
                @NotBlank String fullName,
                @NotBlank @Email String email,
                @NotBlank String phone,
                @NotBlank @Size(min = 8) String password) {

        @Override
        public String toString() {
                return "PersonRequest[fullName=" + fullName + ", email=" + email
                                + ", phone=" + phone + ", password=***]";
        }
}
