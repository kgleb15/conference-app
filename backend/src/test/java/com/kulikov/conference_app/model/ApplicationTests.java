package com.kulikov.conference_app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class ApplicationTests {

    private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");

    private Person owner() {
        PersonRequest req = new PersonRequest("Иванов Иван Иванович", "ПрИн-467",
                "abc123@yandex.ru", "88000000000");
        return new Person(req, PersonRole.PARTICIPANT);
    }

    private Person organizer() {
        PersonRequest req = new PersonRequest("Сидоров Сидор Сидорович", "org",
                "org@yandex.ru", "80000000000");
        return new Person(req, PersonRole.ORGANIZER);
    }

    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Текст тезиса");
    }

    @Test
    void test_constructor() {
        Person owner = owner();
        Application a = new Application(owner, request("Тема"));

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

        assertNotNull(a.getId());
        assertFalse(a.getId().isBlank());
        assertNotNull(a.getCreatedAt());
        assertEquals(a.getCreatedAt(), a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());

        assertNotNull(a.getOwner());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
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
        Person owner = owner();
        Application a = new Application(owner, request("Старая"));
        Instant createdAt = a.getCreatedAt();

        a.update(new ApplicationRequest("Новая", "Новый текст"));

        assertEquals("Новая", a.getTitle());
        assertEquals("Новый текст", a.getThesisText());

        assertNotNull(a.getOwner());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
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
        Person owner = owner();
        Application a = new Application(owner, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
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
        Person owner = owner();
        Application a = new Application(owner, request("Тема"));
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
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());

        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNotNull(a.getWithdrawnAt());
    }

    @Test
    void test_update_incrementsEditCount() {
        Person owner = owner();
        Application a = new Application(owner, request("Тема"));

        a.update(new ApplicationRequest("Новая", "Новый текст"));
        assertEquals(1, a.getEditCount());
        assertNotNull(a.getLastEditedAt());

        a.update(new ApplicationRequest("Ещё", "Ещё текст"));
        assertEquals(2, a.getEditCount());

        a.withdraw();
        assertEquals(2, a.getEditCount());
        assertNotNull(a.getWithdrawnAt());

        assertEquals("Ещё", a.getTitle());
        assertEquals("Ещё текст", a.getThesisText());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());
    }

    @Test
    void test_approve() {
        Person owner = owner();
        Person org = organizer();
        Application a = new Application(owner, request("Тема"));
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
        Application a = new Application(owner(), request("Тема"));

        a.reject(org, "Не по теме", NOW);

        assertEquals(ApplicationStatus.REJECTED, a.getStatus());
        assertSame(org, a.getDecidedBy());
        assertEquals("Не по теме", a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());
        assertEquals(0, a.getEditCount());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
        assertFalse(a.canBeEdited());
        assertFalse(a.canBeModerated());
    }

    @Test
    void test_requestRevision() {
        Person org = organizer();
        Application a = new Application(owner(), request("Тема"));

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

        assertTrue(a.canBeEdited());
        assertFalse(a.canBeModerated());
    }

    @Test
    void test_decision_commentNull() {
        Application a = new Application(owner(), request("Тема"));

        a.approve(organizer(), null, NOW);

        assertEquals(ApplicationStatus.APPROVED, a.getStatus());
        assertNull(a.getModerationComment());
        assertEquals(NOW, a.getDecidedAt());
        assertEquals(NOW, a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(0, a.getEditCount());
        assertNull(a.getLastEditedAt());
        assertNull(a.getWithdrawnAt());
    }

    @Test
    void test_update_afterRevisionRequested() {
        Person org = organizer();
        Application a = new Application(owner(), request("Тема"));
        a.requestRevision(org, "Допишите", NOW);

        a.update(new ApplicationRequest("Доработанная", "Новый текст"));

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals("Доработанная", a.getTitle());
        assertEquals("Новый текст", a.getThesisText());
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
}
