package com.kulikov.conference_app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ApplicationTests {

    private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");

    private static final Direction DIRECTION = new Direction(7, "Программно-информационное обеспечение");
    private static final Direction OTHER_DIRECTION = new Direction(8, "Экономика и управление");

    private static final List<SupervisorRequest> SUPERVISORS = List.of(
        new SupervisorRequest("Дроботов Алексей Владимирович", "ВолгГТУ, каф. АПП"));
        
    private Participant owner() {
        ParticipantRequest req = new ParticipantRequest("Иванов Иван Иванович",
                "abc123@yandex.ru", "88000000000", "password123");
        return new Participant(req, "hash");
    }

    private Organizer organizer() {
        StaffRequest req = new StaffRequest("Сидоров Сидор Сидорович",
                "org@yandex.ru", "password123", 7);
        return new Organizer(req, "hash", DIRECTION);
    }

    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Аннотация", "Текст тезиса", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, 7, SUPERVISORS, null);
    }

    @Test
    void test_constructor() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Тема"));

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

        assertNotNull(a.getId());
        assertFalse(a.getId().isBlank());
        assertNotNull(a.getCreatedAt());
        assertEquals(a.getCreatedAt(), a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());

        assertNotNull(a.getOwner());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(ApplicantCategory.STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());
        assertEquals(PersonRole.PARTICIPANT, a.getOwner().getRole());

        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());

        assertNull(a.getDecidedBy());
        assertNull(a.getModerationComment());
        assertNull(a.getDecidedAt());
        assertTrue(a.canBeEdited());
        assertTrue(a.canBeModerated());
    }

    @Test
    void test_update_allFields() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Старая"));
        Instant createdAt = a.getCreatedAt();

        a.update(new ApplicationRequest("Новая", "Новая аннотация", "Новый текст", "ПрИн-468",
                ApplicantCategory.COLLEGE_STUDENT, FundingType.CONTRACT, 7, SUPERVISORS, null));

        assertEquals("Новая", a.getTitle());
        assertEquals("Новый текст", a.getThesisText());
        assertEquals("Новая аннотация", a.getAnnotation());
        assertEquals("ПрИн-468", a.getGroupName());
        assertEquals(ApplicantCategory.COLLEGE_STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.CONTRACT, a.getFundingType());

        assertNotNull(a.getOwner());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());

        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

        assertEquals(1, a.getEditCount());
        assertNotNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
    }

    @Test
    void test_withdraw_changesStatusToWithdrawn() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(ApplicantCategory.STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());

        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));

        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNotNull(a.getWithdrawnAt());
    }

    @Test
    void test_withdraw_doubleTimes() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.withdraw();

        Instant firstWithdrawnAt = a.getWithdrawnAt();
        Instant firstUpdatedAt = a.getUpdatedAt();
        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());
        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));

        a.withdraw();

        assertEquals(firstWithdrawnAt, a.getWithdrawnAt());
        assertEquals(firstUpdatedAt, a.getUpdatedAt());
        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());
        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(ApplicantCategory.STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());

        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNotNull(a.getWithdrawnAt());
    }

    @Test
    void test_update_incrementsEditCount() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Тема"));

        a.update(new ApplicationRequest("Новая", "Новая аннотация", "Новый текст", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, 7, SUPERVISORS, null));
        assertEquals(1, a.getEditCount());
        assertNotNull(a.getLastEditedAt());

        a.update(new ApplicationRequest("Ещё", "Новая аннотация", "Ещё текст", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, 7, SUPERVISORS, null));
        assertEquals(2, a.getEditCount());

        a.withdraw();
        assertEquals(2, a.getEditCount());
        assertNotNull(a.getWithdrawnAt());

        assertEquals("Ещё", a.getTitle());
        assertEquals("Ещё текст", a.getThesisText());
        assertEquals("Новая аннотация", a.getAnnotation());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(ApplicantCategory.STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());
    }

    @Test
    void test_approve() {
        Participant owner = owner();
        Person org = organizer();
        Application a = new Application(owner, DIRECTION, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.approve(org, "Отлично", NOW);

        assertEquals(ApplicationStatus.APPROVED, a.getStatus());
        assertSame(org, a.getDecidedBy());
        assertEquals(org.getId(), a.getDecidedBy().getId());
        assertEquals(PersonRole.ORGANIZER, a.getDecidedBy().getRole());
        assertEquals("Отлично", a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals(createdAt, a.getCreatedAt());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());

        assertFalse(a.canBeEdited());
        assertFalse(a.canBeModerated());
    }

    @Test
    void test_reject() {
        Person org = organizer();
        Application a = new Application(owner(), DIRECTION, request("Тема"));

        a.reject(org, "Не по теме", NOW);

        assertEquals(ApplicationStatus.REJECTED, a.getStatus());
        assertSame(org, a.getDecidedBy());
        assertEquals("Не по теме", a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());
        assertEquals(0, a.getEditCount());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
        assertFalse(a.canBeEdited());
        assertFalse(a.canBeModerated());
    }

    @Test
    void test_requestRevision() {
        Person org = organizer();
        Application a = new Application(owner(), DIRECTION, request("Тема"));

        a.requestRevision(org, "Допишите введение", NOW);

        assertEquals(ApplicationStatus.REVISION_REQUESTED, a.getStatus());
        assertSame(org, a.getDecidedBy());
        assertEquals("Допишите введение", a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());

        assertTrue(a.canBeEdited());
        assertFalse(a.canBeModerated());
    }

    @Test
    void test_decision_commentNull() {
        Application a = new Application(owner(), DIRECTION, request("Тема"));

        a.approve(organizer(), null, NOW);

        assertEquals(ApplicationStatus.APPROVED, a.getStatus());
        assertNull(a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
    }

    @Test
    void test_update_afterRevisionRequested() {
        Person org = organizer();
        Application a = new Application(owner(), DIRECTION, request("Тема"));
        a.requestRevision(org, "Допишите", NOW);

        a.update(new ApplicationRequest("Доработанная", "Новая аннотация", "Новый текст", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, 7, SUPERVISORS, null));

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals("Доработанная", a.getTitle());
        assertEquals("Новый текст", a.getThesisText());
        assertEquals("Новая аннотация", a.getAnnotation());
        assertEquals(1, a.getEditCount());
        assertNotNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());

        assertSame(org, a.getDecidedBy());
        assertEquals("Допишите", a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());

        assertTrue(a.canBeEdited());
        assertTrue(a.canBeModerated());

        a.approve(org, "Теперь хорошо", NOW.plusSeconds(60));
        assertEquals(ApplicationStatus.APPROVED, a.getStatus());
        assertEquals("Теперь хорошо", a.getModerationComment());
    }

    @Test
    void test_transferTo_changesDirection() {
        Participant owner = owner();
        Application a = new Application(owner, DIRECTION, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.transferTo(OTHER_DIRECTION, NOW);

        assertSame(OTHER_DIRECTION, a.getDirection());
        assertEquals(8, a.getDirection().getId());
        assertEquals("Экономика и управление", a.getDirection().getName());
        assertEquals(NOW, a.getUpdatedAt());
        assertEquals(createdAt, a.getCreatedAt());

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Аннотация", a.getAnnotation());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(ApplicantCategory.STUDENT, a.getApplicantCategory());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertSame(owner, a.getOwner());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
        assertNull(a.getDecidedBy());
        assertNull(a.getModerationComment());
        assertNull(a.getDecidedAt());
    }

    @Test
    void test_transferTo_notAllowedAfterDecision() {
        Organizer org = organizer();
        Application approved = new Application(owner(), DIRECTION, request("Принятая"));
        approved.approve(org, "Принята", NOW);

        assertSame(DIRECTION, approved.getDirection());
        assertEquals(ApplicationStatus.APPROVED, approved.getStatus());
        assertEquals(NOW, approved.getUpdatedAt());
        assertSame(org, approved.getDecidedBy());
        assertEquals("Принята", approved.getModerationComment());

        Application withdrawn = new Application(owner(), DIRECTION, request("Отозванная"));
        withdrawn.withdraw();

        assertThrows(IllegalStateException.class, () -> withdrawn.transferTo(OTHER_DIRECTION, NOW));
        assertSame(DIRECTION, withdrawn.getDirection());
        assertEquals(ApplicationStatus.WITHDRAWN, withdrawn.getStatus());
    }
}
