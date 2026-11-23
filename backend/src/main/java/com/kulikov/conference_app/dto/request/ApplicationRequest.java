package com.kulikov.conference_app.dto.request;

import com.kulikov.conference_app.model.application.ApplicantCategory;
import com.kulikov.conference_app.model.application.FundingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ApplicationRequest(
        @NotBlank String title,
        @NotBlank String annotation,
        @NotBlank String thesisText,
        @NotBlank String groupName,
        @NotNull ApplicantCategory applicantCategory,
        @NotNull FundingType fundingType,
        @NotNull Integer directionId,
        @NotEmpty @Size(max = 2) @Valid List<@NotNull SupervisorRequest> supervisors,
        @Valid CoAuthorRequest coAuthor) {
}
