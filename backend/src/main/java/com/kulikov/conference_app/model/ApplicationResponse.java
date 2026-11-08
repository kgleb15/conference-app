package com.kulikov.conference_app.model;

import java.time.Instant;

/**
 * Ответ API с данными заявки
 */
public record ApplicationResponse(String id, String title, String annotation, String thesisText,
        String groupName, ApplicantCategory applicantCategory, FundingType fundingType,
        ApplicationStatus status, Instant createdAt, Instant updatedAt,
        int editCount, Instant lastEditedAt, Instant withdrawnAt, PersonResponse owner,
        String moderationComment, Instant decidedAt, String decidedByName) {

    /**
     * Собирает ответ из сущности Application
     */
    public static ApplicationResponse from(Application a) {
        String decidedByName = a.getDecidedBy() == null ? null : a.getDecidedBy().getFullName();
        return new ApplicationResponse(a.getId(), a.getTitle(), a.getAnnotation(), a.getThesisText(),
                a.getGroupName(), a.getApplicantCategory(), a.getFundingType(),
                a.getStatus(), a.getCreatedAt(), a.getUpdatedAt(),
                a.getEditCount(), a.getLastEditedAt(), a.getWithdrawnAt(), PersonResponse.from(a.getOwner()),
                a.getModerationComment(), a.getDecidedAt(), decidedByName);
    }
}
