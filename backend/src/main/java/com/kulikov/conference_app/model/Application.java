package com.kulikov.conference_app.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Entity
public class Application {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(updatable = false)
    private final Instant createdAt = Instant.now();
    private Instant updatedAt = createdAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    private String title;

    @Column(columnDefinition = "text")
    private String annotation;

    @Column(columnDefinition = "text")
    private String thesisText;

    @Enumerated(EnumType.STRING)
    private ApplicantCategory applicantCategory;

    private int editCount = 0;

    private Instant lastEditedAt;
    private Instant withdrawnAt;

    @ManyToOne
    private Person decidedBy; // организатор, принявший решение

    @Column(columnDefinition = "text")
    private String moderationComment;

    private Instant decidedAt;

    @ManyToOne(optional = false)
    private Direction direction;

    @ManyToOne
    private Direction transferTarget; // направление, куда заявку передают

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("authorIndex")
    private List<ApplicationAuthor> authors = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "supervisor1_full_name")),
            @AttributeOverride(name = "workplace", column = @Column(name = "supervisor1_workplace")) })
    private Supervisor supervisor1;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "supervisor2_full_name")),
            @AttributeOverride(name = "workplace", column = @Column(name = "supervisor2_workplace")) })
    private Supervisor supervisor2;

    protected Application() {
    }

    public Application(Participant owner, Direction direction, ApplicationRequest r) {
        this(owner, null, direction, r);
    }

    public Application(Participant owner, Participant coAuthor, Direction direction, ApplicationRequest r) {
        this.direction = direction;
        authors.add(new ApplicationAuthor(this, owner, 0, r.groupName(), r.fundingType()));
        if (coAuthor != null) {
            authors.add(new ApplicationAuthor(this, coAuthor, 1, r.coAuthor().groupName(), r.coAuthor().fundingType()));
        }
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
        authors.get(0).change(r.groupName(), r.fundingType());
        if (r.coAuthor() != null && authors.size() > 1) {
            authors.get(1).change(r.coAuthor().groupName(), r.coAuthor().fundingType());
        }
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

        transferTarget = null; // если заявка передавалась передача отменяется
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
        annotation = r.annotation();
        thesisText = r.thesisText();
        applicantCategory = r.applicantCategory();
        supervisor1 = toSupervisor(r.supervisors().get(0));
        supervisor2 = r.supervisors().size() > 1 ? toSupervisor(r.supervisors().get(1)) : null;
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
     * Начинает передачу заявки в другое направление
     *
     * @param target - направление, в которое передаём
     * @param now    - время передачи
     */
    public void requestTransfer(Direction target, Instant now) {
        if (!canBeModerated()) {
            throw new IllegalStateException("Заявку в статусе " + status + " нельзя перевести");
        }
        status = ApplicationStatus.TRANSFER_PENDING;
        transferTarget = target;
        updatedAt = now;
    }

    /**
     * Принимает передачу заявки
     */
    public void acceptTransfer(Instant now) {
        if (status != ApplicationStatus.TRANSFER_PENDING) {
            throw new IllegalStateException("Заявка в статусе " + status + " не передаётся");
        }
        direction = transferTarget;
        transferTarget = null;
        status = ApplicationStatus.SUBMITTED;
        updatedAt = now;
    }

    /**
     * Отменяет передачу
     */
    public void cancelTransfer(Instant now) {
        if (status != ApplicationStatus.TRANSFER_PENDING) {
            throw new IllegalStateException("Заявка в статусе " + status + " не передаётся");
        }
        transferTarget = null;
        status = ApplicationStatus.SUBMITTED;
        updatedAt = now;
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

    private static Supervisor toSupervisor(SupervisorRequest s) {
        return new Supervisor(s.fullName(), s.workplace());
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

    public String getAnnotation() {
        return annotation;
    }

    public String getThesisText() {
        return thesisText;
    }

    public ApplicantCategory getApplicantCategory() {
        return applicantCategory;
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

    public Direction getDirection() {
        return direction;
    }

    public Direction getTransferTarget() {
        return transferTarget;
    }

    public List<Supervisor> getSupervisors() {
        List<Supervisor> result = new ArrayList<>();
        result.add(supervisor1);
        if (supervisor2 != null) {
            result.add(supervisor2);
        }
        return result;
    }

    /**
     * Возвращает автора, подавшего заявку
     */
    public Participant getOwner() {
        return authors.get(0).getPerson();
    }

    public String getGroupName() {
        return authors.get(0).getGroupName();
    }

    public FundingType getFundingType() {
        return authors.get(0).getFundingType();
    }

    public List<ApplicationAuthor> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    /**
     * Находит запись автора для данного человека
     */
    public Optional<ApplicationAuthor> authorOf(Person person) {
        for (ApplicationAuthor author : authors) {
            if (author.getPerson().getId().equals(person.getId())) {
                return Optional.of(author);
            }
        }
        return Optional.empty();
    }

    public boolean isAuthor(Person person) {
        return authorOf(person).isPresent();
    }

    public boolean isOwner(Person person) {
        return getOwner().getId().equals(person.getId());
    }
}
