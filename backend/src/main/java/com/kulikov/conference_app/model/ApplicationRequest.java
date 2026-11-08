package com.kulikov.conference_app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotBlank String title,
        @NotBlank String annotation,
        @NotBlank String thesisText,
        @NotBlank String groupName,
        @NotNull ApplicantCategory applicantCategory,
        @NotNull FundingType fundingType) {
}
