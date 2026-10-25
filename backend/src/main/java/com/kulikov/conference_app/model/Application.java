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

    @ManyToOne
    private Person decidedBy; // организатор, принявший решение

    @Column(columnDefinition = "text")
    private String moderationComment;

    private Instant decidedAt;

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
        if (!canBeEdited()) {
            throw new IllegalStateException("Заявку в статусе " + status + " нельзя редактировать");
        }
        apply(r);
        if (status == ApplicationStatus.REVISION_REQUESTED) {
            status = ApplicationStatus.SUBMITTED; // доработана, снова ждёт организатора
        }
        updatedAt = Instant.now();
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
        if (status == ApplicationStatus.APPROVED || status == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Заявку в статусе " + status + " нельзя отозвать");
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

    /**
     * Можно ли редактировать заявку
     */
    public boolean canBeEdited() {
        return status == ApplicationStatus.SUBMITTED || status == ApplicationStatus.REVISION_REQUESTED;
    }

    /**
     * Можно ли принять решение по заявке
     */
    public boolean canBeModerated() {
        return status == ApplicationStatus.SUBMITTED;
    }

    /**
     * Утверждает заявку
     *
     * @param organizer - организатор, принявший решение
     * @param comment   - комментарий
     * @param now       - время решения
     */
    public void approve(Person organizer, String comment, Instant now) {
        decide(ApplicationStatus.APPROVED, organizer, comment, now);
    }

    /**
     * Отклоняет заявку
     * 
     * @param organizer - организатор, принявший решение
     * @param comment   - комментарий
     * @param now       - время решения
     */
    public void reject(Person organizer, String comment, Instant now) {
        decide(ApplicationStatus.REJECTED, organizer, comment, now);
    }

    /**
     * Отправляет заявку на доработку
     * 
     * @param organizer - организатор, принявший решение
     * @param comment   - комментарий
     * @param now       - время решения
     */
    public void requestRevision(Person organizer, String comment, Instant now) {
        decide(ApplicationStatus.REVISION_REQUESTED, organizer, comment, now);
    }

    private void decide(ApplicationStatus newStatus, Person organizer, String comment, Instant now) {
        if (!canBeModerated()) {
            throw new IllegalStateException("Заявку в статусе " + status + " нельзя модерировать");
        }
        status = newStatus;
        decidedBy = organizer;
        moderationComment = comment;
        decidedAt = now;
        updatedAt = now;
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

    public Person getDecidedBy() {
        return decidedBy;
    }

    public String getModerationComment() {
        return moderationComment;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
