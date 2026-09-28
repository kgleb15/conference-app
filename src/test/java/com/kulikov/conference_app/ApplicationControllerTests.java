package com.kulikov.conference_app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApplicationControllerTests {
    private final ApplicationController controller = new ApplicationController();

    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Текст тезиса", "Иванов Иван Иванович",
                "ПрИн-467", "abc123@yandex.ru", "88000000000");
    }

    private Application create(String title) {
        return controller.postApplication(request(title)).getBody();
    }

    /** Считает количество заявок в контроллере. */
    private int count() {
        int n = 0;
        for (Application i : controller.getApplications()) {
            n++;
        }
        return n;
    }

    // ---------------- GET ----------------

    @Test
    void test_getApplications() {
        assertEquals(0, count());

        Application first = create("Первая");
        create("Вторая");
        create("Третья");

        assertEquals(3, count());

        Optional<Application> found = controller.getApplication(first.getId());
        Application a = found.get();
        assertEquals(first.getId(), a.getId());
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals(first.getCreatedAt(), a.getCreatedAt());
        assertEquals(first.getUpdatedAt(), a.getUpdatedAt());
        assertEquals("Первая", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());
    }

    @Test
    void test_getApplication() {
        Application created = create("Тема");

        Optional<Application> found = controller.getApplication(created.getId());

        assertTrue(found.isPresent());
        Application a = found.get();

        assertEquals(created.getId(), a.getId());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

        Optional<Application> found2 = controller.getApplication("0987654321");
        assertTrue(found2.isEmpty());
    }

    // ---------------- POST ----------------

    @Test
    void test_postApplication_returns201() {
        ResponseEntity<Application> response = controller.postApplication(request("Тема"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals("Тема", response.getBody().getTitle());
        assertEquals(ApplicationStatus.SUBMITTED, response.getBody().getStatus());

        assertEquals("Текст тезиса", response.getBody().getThesisText());
        assertEquals("Иванов Иван Иванович", response.getBody().getAuthorFullName());
        assertEquals("ПрИн-467", response.getBody().getAuthorGroup());
        assertEquals("abc123@yandex.ru", response.getBody().getAuthorEmail());
        assertEquals("88000000000", response.getBody().getAuthorPhone());

        assertEquals(1, count());
    }

    // ---------------- PUT ----------------

    @Test
    void test_putApplication_updatesExisting() {
        Application created = create("Старая");

        ResponseEntity<Application> response = controller.putApplication(created.getId(), request("Новая"));

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals(created.getId(), response.getBody().getId());
        assertEquals("Новая", response.getBody().getTitle());
        assertEquals(ApplicationStatus.SUBMITTED, response.getBody().getStatus());

        assertEquals("Текст тезиса", response.getBody().getThesisText());
        assertEquals("Иванов Иван Иванович", response.getBody().getAuthorFullName());
        assertEquals("ПрИн-467", response.getBody().getAuthorGroup());
        assertEquals("abc123@yandex.ru", response.getBody().getAuthorEmail());
        assertEquals("88000000000", response.getBody().getAuthorPhone());

        assertEquals(1, count());
    }

    @Test
    void test_putApplication_createsWhenNotFound() {
        ResponseEntity<Application> response = controller.putApplication("0987654321", request("Новая"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новая", response.getBody().getTitle());
        assertEquals(1, count());

        assertEquals("Текст тезиса", response.getBody().getThesisText());
        assertEquals("Иванов Иван Иванович", response.getBody().getAuthorFullName());
        assertEquals("ПрИн-467", response.getBody().getAuthorGroup());
        assertEquals("abc123@yandex.ru", response.getBody().getAuthorEmail());
        assertEquals("88000000000", response.getBody().getAuthorPhone());

        assertEquals(1, count());

        ResponseEntity<Application> response2 = controller.putApplication("1234567890", request("Еще одна"));

        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals("Еще одна", response2.getBody().getTitle());
        assertEquals(2, count());
    }

    @Test
    void test_putApplication_withdrawnReturns409() {
        Application created = create("Тема");
        controller.withdraw(created.getId());

        Optional<Application> app = controller.getApplication(created.getId());
        assertTrue(app.isPresent());
        Application a = app.get();

        assertEquals(created.getId(), a.getId());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
        assertEquals("ПрИн-467", a.getAuthorGroup());
        assertEquals("abc123@yandex.ru", a.getAuthorEmail());
        assertEquals("88000000000", a.getAuthorPhone());
        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

        ResponseEntity<Application> response = controller.putApplication(created.getId(), request("Другая"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(1, count());
    }

    // ---------------- PATCH ----------------

    @Test
    void test_withdraw_changesStatusToWithdrawn() {
        Application created = create("Тема");

        ResponseEntity<Application> response = controller.withdraw(created.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApplicationStatus.WITHDRAWN, response.getBody().getStatus());

        assertEquals("Текст тезиса", response.getBody().getThesisText());
        assertEquals("Иванов Иван Иванович", response.getBody().getAuthorFullName());
        assertEquals("ПрИн-467", response.getBody().getAuthorGroup());
        assertEquals("abc123@yandex.ru", response.getBody().getAuthorEmail());
        assertEquals("88000000000", response.getBody().getAuthorPhone());
        assertEquals(1, count());
    }

    @Test
    void test_withdraw_unknownId404() {
        ResponseEntity<Application> response = controller.withdraw("0101010101");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(0, count());
    }

    @Test
    void test_withdraw_doubleTimes() {
        Application created = create("Тема");

        ResponseEntity<Application> response = controller.withdraw(created.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApplicationStatus.WITHDRAWN, response.getBody().getStatus());

        ResponseEntity<Application> response2 = controller.withdraw(created.getId());

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals(ApplicationStatus.WITHDRAWN, response2.getBody().getStatus());

        assertEquals("Текст тезиса", response.getBody().getThesisText());
        assertEquals("Иванов Иван Иванович", response.getBody().getAuthorFullName());
        assertEquals("ПрИн-467", response.getBody().getAuthorGroup());
        assertEquals("abc123@yandex.ru", response.getBody().getAuthorEmail());
        assertEquals("88000000000", response.getBody().getAuthorPhone());
        assertEquals(1, count());
    }
}
