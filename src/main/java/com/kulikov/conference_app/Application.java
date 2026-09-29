package com.kulikov.conference_app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Application {
    @Id
    private String _id = UUID.randomUUID().toString();

    private final Instant _createdAt = Instant.now();
    private Instant _updatedAt = _createdAt;
    private ApplicationStatus _status = ApplicationStatus.SUBMITTED;

    private String _title;
    private String _thesisText;
    private String _authorFullName;
    private String _authorGroup;
    private String _authorEmail;
    private String _authorPhone;

    protected Application() {
    }

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
        _updatedAt = Instant.now(); // устанавливаем время обновления
    }

    /**
     * Отзыв заявки
     */
    public void withdraw() {
        _status = ApplicationStatus.WITHDRAWN;
        _updatedAt = Instant.now();
    }

    /**
     * Переносит поля в объект заявки
     * 
     * @param r - данные заявки
     */
    private void apply(ApplicationRequest r) {
        _title = r.title();
        _thesisText = r.thesisText();
        _authorFullName = r.authorFullName();
        _authorGroup = r.authorGroup();
        _authorEmail = r.authorEmail();
        _authorPhone = r.authorPhone();
    }

    // ------------- Геттеры ----------------

    public String getId() {
        return _id;
    }

    public Instant getCreatedAt() {
        return _createdAt;
    }

    public Instant getUpdatedAt() {
        return _updatedAt;
    }

    public ApplicationStatus getStatus() {
        return _status;
    }

    public String getTitle() {
        return _title;
    }

    public String getThesisText() {
        return _thesisText;
    }

    public String getAuthorFullName() {
        return _authorFullName;
    }

    public String getAuthorGroup() {
        return _authorGroup;
    }

    public String getAuthorEmail() {
        return _authorEmail;
    }

    public String getAuthorPhone() {
        return _authorPhone;
    }
}
