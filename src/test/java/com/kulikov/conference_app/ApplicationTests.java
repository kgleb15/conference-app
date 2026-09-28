package com.kulikov.conference_app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class ApplicationTests {
    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Текст тезиса", "Иванов Иван Иванович",
                "ПрИн-467", "abc123@yandex.ru", "88000000000");
    }

    @Test
    void test_constructor() {
        Application a = new Application(request("Тема"));

        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

        assertNotNull(a.getId());
        assertFalse(a.getId().isBlank());
        assertNotNull(a.getCreatedAt());
        assertEquals(a.getCreatedAt(), a.getUpdatedAt());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());
    }

    @Test
    void test_update_allFields() {
        Application a = new Application(request("Старая"));
        Instant createdAt = a.getCreatedAt();

        a.update(new ApplicationRequest("Новая", "Новый текст", "Петров Пётр Петрович",
                "ПрИн-466", "def456@yandex.ru", "88888888888"));

        assertEquals("Новая", a.getTitle());
        assertEquals("Новый текст", a.getThesisText());
        assertEquals("Петров Пётр Петрович", a.getAuthorFullName());
        assertEquals("ПрИн-466", a.getAuthorGroup());
        assertEquals("def456@yandex.ru", a.getAuthorEmail());
        assertEquals("88888888888", a.getAuthorPhone());

        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
    }

    @Test
    void test_withdraw_changesStatusToWithdrawn() {
        Application a = new Application(request("Тема"));
        Instant createdAt = a.getCreatedAt();

        a.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());

        assertEquals(createdAt, a.getCreatedAt());
        assertFalse(a.getUpdatedAt().isBefore(createdAt));
    }

    @Test
    void test_withdraw_doubleTimes() {
        Application a = new Application(request("Тема"));
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
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());
    }
}
