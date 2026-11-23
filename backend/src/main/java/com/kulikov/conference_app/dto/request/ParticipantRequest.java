package com.kulikov.conference_app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipantRequest(
                @NotBlank String fullName,
                @NotBlank @Email String email,
                @NotBlank String phone,
                @NotBlank @Size(min = 8) String password) {

        @Override
        public String toString() {
                return "ParticipantRequest[fullName=" + fullName + ", email=" + email
                                + ", phone=" + phone + ", password=***]";
        }
}
