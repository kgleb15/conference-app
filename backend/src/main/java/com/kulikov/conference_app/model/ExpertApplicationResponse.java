package com.kulikov.conference_app.model;

/**
 * Ответ API для эксперта. Содержит только публичные поля заявки
 */
public record ExpertApplicationResponse(String id, String title, String annotation, String thesisText,
        ApplicantCategory applicantCategory) {

    public static ExpertApplicationResponse from(Application a) {
        return new ExpertApplicationResponse(a.getId(), a.getTitle(), a.getAnnotation(), a.getThesisText(),
                a.getApplicantCategory());
    }
}
