package com.kulikov.conference_app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kulikov.conference_app.auth.CurrentUser;
import com.kulikov.conference_app.config.ConferenceProperties;
import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.repositories.InMemoryApplicationRepository;
import com.kulikov.conference_app.repositories.InMemoryPersonRepository;

public class ApplicationServiceTests {

    private static final Instant DEADLINE = Instant.parse("2026-03-15T23:59:59Z");
    private static final String BEFORE = "2026-03-10T12:00:00Z";
    private static final String AFTER = "2026-03-16T00:00:00Z";

    private final InMemoryApplicationRepository _appRepo = new InMemoryApplicationRepository();
    private final InMemoryPersonRepository _personRepo = new InMemoryPersonRepository();

    private ApplicationService serviceAt(String now, String personId) {
        ConferenceProperties props = new ConferenceProperties();
        props.setDeadlineT1(DEADLINE);
        Clock clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC);
        CurrentUser currentUser = () -> Optional.ofNullable(personId);
        return new ApplicationService(_appRepo, _personRepo, currentUser, props, clock);
    }

    /** Регистрирует участника и возвращает его сущность */
    private Person registerPerson(String fullName) {
        PersonRequest req = new PersonRequest(fullName, "ПрИн-467",
                "abc123@yandex.ru", "88000000000");
        return _personRepo.save(new Person(req, PersonRole.PARTICIPANT));
    }

    /** Создаёт запрос заявки с заданным заголовком */
    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Текст тезиса");
    }

    private Application create(ApplicationService service, String title) {
        return service.submit(request(title)).application();
    }

    private int count(ApplicationService service) {
        int n = 0;
        for (Application a : service.findAll()) {
            n++;
        }
        return n;
    }

    // ---------------- GET ----------------

    @Test
    void test_getApplications() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        assertEquals(0, count(service));

        Application first = create(service, "Первая");
        create(service, "Вторая");
        create(service, "Третья");

        assertEquals(3, count(service));

        ServiceResult result = service.get(first.getId());
        assertEquals(ServiceStatus.OK, result.status());

        Application a = result.application();
        assertEquals(first.getId(), a.getId());
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals(first.getCreatedAt(), a.getCreatedAt());
        assertEquals(first.getUpdatedAt(), a.getUpdatedAt());
        assertEquals("Первая", a.getTitle());
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
    void test_getApplication() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Тема");

        ServiceResult result = service.get(created.getId());

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());

        Application a = result.application();
        assertEquals(created.getId(), a.getId());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());

        ServiceResult missing = service.get("0987654321");
        assertEquals(ServiceStatus.NOT_FOUND, missing.status());
        assertNull(missing.application());
    }

    // ---------------- POST ----------------

    @Test
    void test_submit_returnsCreated() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());

        ServiceResult result = service.submit(request("Тема"));

        assertEquals(ServiceStatus.CREATED, result.status());
        assertNotNull(result.application());
        assertEquals("Тема", result.application().getTitle());
        assertEquals("Текст тезиса", result.application().getThesisText());
        assertEquals(ApplicationStatus.SUBMITTED, result.application().getStatus());

        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals("Иванов Иван Иванович", result.application().getOwner().getFullName());
        assertEquals("ПрИн-467", result.application().getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", result.application().getOwner().getEmail());
        assertEquals("88000000000", result.application().getOwner().getPhone());

        assertEquals(1, count(service));
    }

    @Test
    void test_submit_afterDeadline() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(AFTER, owner.getId());

        ServiceResult result = service.submit(request("Тема"));

        assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
        assertNull(result.application());
        assertEquals(0, count(service));
    }

    @Test
    void test_submit_noCurrentUser() {
        ApplicationService service = serviceAt(BEFORE, null);

        ServiceResult result = service.submit(request("Тема"));

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.application());
        assertEquals(0, count(service));
    }

    @Test
    void test_submit_unknownCurrentUser() {
        ApplicationService service = serviceAt(BEFORE, "123");

        ServiceResult result = service.submit(request("Тема"));

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.application());
        assertEquals(0, count(service));
    }

    // ---------------- PUT ----------------

    @Test
    void test_edit_updatesExisting() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Старая");

        ServiceResult result = service.edit(created.getId(), request("Новая"));

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        assertEquals(created.getId(), result.application().getId());
        assertEquals("Новая", result.application().getTitle());
        assertEquals("Текст тезиса", result.application().getThesisText());
        assertEquals(ApplicationStatus.SUBMITTED, result.application().getStatus());

        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals("Иванов Иван Иванович", result.application().getOwner().getFullName());
        assertEquals("ПрИн-467", result.application().getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", result.application().getOwner().getEmail());
        assertEquals("88000000000", result.application().getOwner().getPhone());

        assertEquals(1, count(service));
    }

    @Test
    void test_edit_createsWhenNotFound() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());

        ServiceResult result = service.edit("0987654321", request("Новая"));

        assertEquals(ServiceStatus.CREATED, result.status());
        assertNotNull(result.application());
        assertEquals("Новая", result.application().getTitle());
        assertEquals("Текст тезиса", result.application().getThesisText());
        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals(1, count(service));

        ServiceResult result2 = service.edit("1234567890", request("Еще одна"));

        assertEquals(ServiceStatus.CREATED, result2.status());
        assertNotNull(result2.application());
        assertEquals("Еще одна", result2.application().getTitle());
        assertEquals(2, count(service));
    }

    @Test
    void test_edit_withdrawn() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Тема");
        service.withdraw(created.getId());

        ServiceResult found = service.get(created.getId());
        assertEquals(ServiceStatus.OK, found.status());
        Application a = found.application();

        assertEquals(created.getId(), a.getId());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals("Иванов Иван Иванович", a.getOwner().getFullName());
        assertEquals("ПрИн-467", a.getOwner().getGroupName());
        assertEquals("abc123@yandex.ru", a.getOwner().getEmail());
        assertEquals("88000000000", a.getOwner().getPhone());
        assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

        ServiceResult result = service.edit(created.getId(), request("Другая"));

        assertEquals(ServiceStatus.WITHDRAWN, result.status());
        assertNull(result.application());
        assertEquals(1, count(service));
    }

    @Test
    void test_edit_afterDeadline() {
        Person owner = registerPerson("Иванов Иван Иванович");

        ApplicationService before = serviceAt(BEFORE, owner.getId());
        Application created = create(before, "Тема");

        ApplicationService after = serviceAt(AFTER, owner.getId());
        ServiceResult result = after.edit(created.getId(), request("Новая"));

        assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
        assertNull(result.application());

        ServiceResult stored = after.get(created.getId());
        assertEquals("Тема", stored.application().getTitle());
        assertEquals("Текст тезиса", stored.application().getThesisText());
        assertEquals(owner.getId(), stored.application().getOwner().getId());
        assertEquals(1, count(after));
    }

    // ---------------- PATCH ----------------

    @Test
    void test_withdraw_changesStatusToWithdrawn() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Тема");

        ServiceResult result = service.withdraw(created.getId());

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        assertEquals(ApplicationStatus.WITHDRAWN, result.application().getStatus());
        assertEquals("Текст тезиса", result.application().getThesisText());
        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals(1, count(service));
    }

    @Test
    void test_withdraw_unknownId() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());

        ServiceResult result = service.withdraw("0101010101");

        assertEquals(ServiceStatus.NOT_FOUND, result.status());
        assertNull(result.application());
        assertEquals(0, count(service));
    }

    @Test
    void test_withdraw_doubleTimes() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Тема");

        ServiceResult result = service.withdraw(created.getId());

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        assertEquals(ApplicationStatus.WITHDRAWN, result.application().getStatus());

        ServiceResult result2 = service.withdraw(created.getId());

        assertEquals(ServiceStatus.OK, result2.status());
        assertNotNull(result2.application());
        assertEquals(ApplicationStatus.WITHDRAWN, result2.application().getStatus());

        assertEquals("Текст тезиса", result2.application().getThesisText());
        assertEquals(owner.getId(), result2.application().getOwner().getId());
        assertEquals(1, count(service));
    }

    @Test
    void test_withdraw_afterDeadline() {
        Person owner = registerPerson("Иванов Иван Иванович");

        ApplicationService before = serviceAt(BEFORE, owner.getId());
        Application created = create(before, "Тема");

        ApplicationService after = serviceAt(AFTER, owner.getId());
        ServiceResult result = after.withdraw(created.getId());

        assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
        assertNull(result.application());

        ServiceResult stored = after.get(created.getId());
        assertEquals(ApplicationStatus.SUBMITTED, stored.application().getStatus());
        assertEquals("Текст тезиса", stored.application().getThesisText());
        assertEquals(owner.getId(), stored.application().getOwner().getId());
        assertEquals(1, count(after));
    }

    @Test
    void test_withdraw_noCurrentUser() {
        Person owner = registerPerson("Иванов Иван Иванович");
        ApplicationService withUser = serviceAt(BEFORE, owner.getId());
        Application created = create(withUser, "Тема");

        ApplicationService withoutUser = serviceAt(BEFORE, null);
        ServiceResult result = withoutUser.withdraw(created.getId());

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.application());
    }
}
