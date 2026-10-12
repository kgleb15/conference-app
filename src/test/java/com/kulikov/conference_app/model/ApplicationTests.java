package com.kulikov.conference_app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class ApplicationTests {

    private Person owner() {
        PersonRequest req = new PersonRequest("Иванов Иван Иванович", "ПрИн-467",
                "abc123@yandex.ru", "88000000000");
        return new Person(req, PersonRole.PARTICIPANT);
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
    }

    @Test
    void test_withdraw_doubleTimes() {
        Person owner = owner();
        Application a = new Application(owner, request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());
        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));

        a.withdraw();

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
    }
}

