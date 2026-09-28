package com.kulikov.conference_app;

import java.time.Instant;
import java.util.UUID;

public class Application {
    private final String id = UUID.randomUUID().toString();
    private final Instant createdAt = Instant.now();
    private Instant updatedAt = createdAt;
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    private String title;
    private String thesisText;
    private String authorFullName;
    private String authorGroup;
    private String authorEmail;
    private String authorPhone;

    public Application(ApplicationRequest r) {
        apply(r);
    }

    /**
     * Обновление заявки
     * 
     * @param r - данные заявки
     */
    public void update(ApplicationRequest r) {
        apply(r);
        updatedAt = Instant.now(); // устанавливаем время обновления
    }

    /**
     * Отзыв заявки
     */
    public void withdraw() {
        status = ApplicationStatus.WITHDRAWN;
        updatedAt = Instant.now();
    }

    /**
     * Переносит поля в объект заявки
     * 
     * @param r - данные заявки
     */
    private void apply(ApplicationRequest r) {
        title = r.title();
        thesisText = r.thesisText();
        authorFullName = r.authorFullName();
        authorGroup = r.authorGroup();
        authorEmail = r.authorEmail();
        authorPhone = r.authorPhone();
    }

    // ------------- Геттеры ----------------

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getThesisText() {
        return thesisText;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public String getAuthorGroup() {
        return authorGroup;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getAuthorPhone() {
        return authorPhone;
    }
}
