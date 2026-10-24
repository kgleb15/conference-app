package com.kulikov.conference_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Application {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(optional = false)
    private Person owner;

    @Column(updatable = false)
    private final Instant createdAt = Instant.now();
    private Instant updatedAt = createdAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    private String title;
    private String thesisText;

    private int editCount = 0;

    private Instant lastEditedAt;
    private Instant withdrawnAt;

    protected Application() {
    }

    public Application(Person owner, ApplicationRequest r) {
        this.owner = owner;
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
        lastEditedAt = updatedAt;
        editCount++;
    }

    /**
     * Отзыв заявки
     */
    public void withdraw() {
        if (status == ApplicationStatus.WITHDRAWN) {
            return; // не перезаписываем время отзыва
        }

        status = ApplicationStatus.WITHDRAWN;
        updatedAt = Instant.now();
        withdrawnAt = updatedAt;
    }

    /**
     * Переносит поля в объект заявки
     * 
     * @param r - данные заявки
     */
    private void apply(ApplicationRequest r) {
        title = r.title();
        thesisText = r.thesisText();
    }

    // ------------- Геттеры ----------------

    public String getId() {
        return id;
    }

    public Person getOwner() {
        return owner;
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

    public int getEditCount() {
        return editCount;
    }

    public Instant getLastEditedAt() {
        return lastEditedAt;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }
}
