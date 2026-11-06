package com.kulikov.conference_app.model;

/**
 * Ответ API для эксперта. Содержит только публичные поля заявки
 */
public record ExpertApplicationResponse(String id, String title, String thesisText) {

    public static ExpertApplicationResponse from(Application a) {
        return new ExpertApplicationResponse(a.getId(), a.getTitle(), a.getThesisText());
    }
}
