package com.kulikov.conference_app.dto.response;

import com.kulikov.conference_app.model.application.ApplicantCategory;
import com.kulikov.conference_app.model.application.Application;
import com.kulikov.conference_app.model.application.ApplicationStatus;

import java.time.Instant;
import java.util.List;

/**
 * Ответ API с данными заявки
 */
public record ApplicationResponse(String id, String title, String annotation, String thesisText,
                                  ApplicantCategory applicantCategory,
                                  ApplicationStatus status, Instant createdAt, Instant updatedAt,
                                  int editCount, Instant lastEditedAt, Instant withdrawnAt,
                                  String moderationComment, Instant decidedAt, String decidedByName, Integer directionId,
                                  List<SupervisorResponse> supervisors, List<AuthorResponse> authors, Integer transferDirectionId) {

    /**
     * Собирает ответ из сущности Application
     */
    public static ApplicationResponse from(Application a) {
        String decidedByName = a.getDecidedBy() == null ? null : a.getDecidedBy().getFullName();
        return new ApplicationResponse(a.getId(), a.getTitle(), a.getAnnotation(), a.getThesisText(),
                a.getApplicantCategory(),
                a.getStatus(), a.getCreatedAt(), a.getUpdatedAt(),
                a.getEditCount(), a.getLastEditedAt(), a.getWithdrawnAt(),
                a.getModerationComment(), a.getDecidedAt(), decidedByName, a.getDirection().getId(),
                a.getSupervisors().stream().map(SupervisorResponse::from).toList(),
                a.getAuthors().stream().map(AuthorResponse::from).toList(),
                a.getTransferTarget() == null ? null : a.getTransferTarget().getId());
    }
}
