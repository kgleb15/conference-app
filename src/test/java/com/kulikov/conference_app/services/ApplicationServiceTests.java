// package com.kulikov.conference_app.services;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;

// import java.time.Clock;
// import java.time.Instant;
// import java.time.ZoneOffset;

// import org.junit.jupiter.api.Test;

// import com.kulikov.conference_app.config.ConferenceProperties;
// import com.kulikov.conference_app.model.Application;
// import com.kulikov.conference_app.model.ApplicationRequest;
// import com.kulikov.conference_app.model.ApplicationStatus;
// import com.kulikov.conference_app.repositories.InMemoryApplicationRepository;

// public class ApplicationServiceTests {

//     private static final Instant DEADLINE = Instant.parse("2026-03-15T23:59:59Z");
//     private static final String BEFORE = "2026-03-10T12:00:00Z";
//     private static final String AFTER = "2026-03-16T00:00:00Z";

//     private final InMemoryApplicationRepository _repository = new InMemoryApplicationRepository();

//     private ApplicationService serviceAt(String now) {
//         ConferenceProperties props = new ConferenceProperties();
//         props.setDeadlineT1(DEADLINE);
//         Clock clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC);
//         return new ApplicationService(_repository, props, clock);
//     }

//     private ApplicationRequest request(String title) {
//         return new ApplicationRequest(title, "Текст тезиса", "Иванов Иван Иванович",
//                 "ПрИн-467", "abc123@yandex.ru", "88000000000");
//     }

//     private Application create(ApplicationService service, String title) {
//         return service.submit(request(title)).application();
//     }

//     private int count(ApplicationService service) {
//         int n = 0;
//         for (Application a : service.findAll()) {
//             n++;
//         }
//         return n;
//     }

//     // ---------------- GET ----------------

//     @Test
//     void test_getApplications() {
//         ApplicationService service = serviceAt(BEFORE);
//         assertEquals(0, count(service));

//         Application first = create(service, "Первая");
//         create(service, "Вторая");
//         create(service, "Третья");

//         assertEquals(3, count(service));

//         ServiceResult result = service.get(first.getId());
//         assertEquals(ServiceStatus.OK, result.status());

//         Application a = result.application();
//         assertEquals(first.getId(), a.getId());
//         assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());
//         assertEquals(first.getCreatedAt(), a.getCreatedAt());
//         assertEquals(first.getUpdatedAt(), a.getUpdatedAt());
//         assertEquals("Первая", a.getTitle());
//         assertEquals("Текст тезиса", a.getThesisText());
//         assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
//         assertEquals("ПрИн-467", a.getAuthorGroup());
//         assertEquals("abc123@yandex.ru", a.getAuthorEmail());
//         assertEquals("88000000000", a.getAuthorPhone());
//     }

//     @Test
//     void test_getApplication() {
//         ApplicationService service = serviceAt(BEFORE);
//         Application created = create(service, "Тема");

//         ServiceResult result = service.get(created.getId());

//         assertEquals(ServiceStatus.OK, result.status());
//         assertNotNull(result.application());

//         Application a = result.application();
//         assertEquals(created.getId(), a.getId());
//         assertEquals("Тема", a.getTitle());
//         assertEquals("Текст тезиса", a.getThesisText());
//         assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
//         assertEquals("ПрИн-467", a.getAuthorGroup());
//         assertEquals("abc123@yandex.ru", a.getAuthorEmail());
//         assertEquals("88000000000", a.getAuthorPhone());
//         assertEquals(ApplicationStatus.SUBMITTED, a.getStatus());

//         ServiceResult missing = service.get("0987654321");
//         assertEquals(ServiceStatus.NOT_FOUND, missing.status());
//         assertNull(missing.application());
//     }

//     // ---------------- POST ----------------

//     @Test
//     void test_submit_returnsCreated() {
//         ApplicationService service = serviceAt(BEFORE);

//         ServiceResult result = service.submit(request("Тема"));

//         assertEquals(ServiceStatus.CREATED, result.status());
//         assertNotNull(result.application());
//         assertEquals("Тема", result.application().getTitle());
//         assertEquals(ApplicationStatus.SUBMITTED, result.application().getStatus());

//         assertEquals("Текст тезиса", result.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", result.application().getAuthorFullName());
//         assertEquals("ПрИн-467", result.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", result.application().getAuthorEmail());
//         assertEquals("88000000000", result.application().getAuthorPhone());

//         assertEquals(1, count(service));
//     }

//     @Test
//     void test_submit_afterDeadline() {
//         ApplicationService service = serviceAt(AFTER);

//         ServiceResult result = service.submit(request("Тема"));

//         assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
//         assertNull(result.application());
//         assertEquals(0, count(service));
//     }

//     // ---------------- PUT ----------------

//     @Test
//     void test_edit_updatesExisting() {
//         ApplicationService service = serviceAt(BEFORE);
//         Application created = create(service, "Старая");

//         ServiceResult result = service.edit(created.getId(), request("Новая"));

//         assertEquals(ServiceStatus.OK, result.status());
//         assertNotNull(result.application());
//         assertEquals(created.getId(), result.application().getId());
//         assertEquals("Новая", result.application().getTitle());
//         assertEquals(ApplicationStatus.SUBMITTED, result.application().getStatus());

//         assertEquals("Текст тезиса", result.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", result.application().getAuthorFullName());
//         assertEquals("ПрИн-467", result.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", result.application().getAuthorEmail());
//         assertEquals("88000000000", result.application().getAuthorPhone());

//         assertEquals(1, count(service));
//     }

//     @Test
//     void test_edit_createsWhenNotFound() {
//         ApplicationService service = serviceAt(BEFORE);

//         ServiceResult result = service.edit("0987654321", request("Новая"));

//         assertEquals(ServiceStatus.CREATED, result.status());
//         assertNotNull(result.application());
//         assertEquals("Новая", result.application().getTitle());
//         assertEquals(1, count(service));

//         assertEquals("Текст тезиса", result.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", result.application().getAuthorFullName());
//         assertEquals("ПрИн-467", result.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", result.application().getAuthorEmail());
//         assertEquals("88000000000", result.application().getAuthorPhone());

//         ServiceResult result2 = service.edit("1234567890", request("Еще одна"));

//         assertEquals(ServiceStatus.CREATED, result2.status());
//         assertNotNull(result2.application());
//         assertEquals("Еще одна", result2.application().getTitle());
//         assertEquals(2, count(service));
//     }

//     @Test
//     void test_edit_withdrawn() {
//         ApplicationService service = serviceAt(BEFORE);
//         Application created = create(service, "Тема");
//         service.withdraw(created.getId());

//         ServiceResult found = service.get(created.getId());
//         assertEquals(ServiceStatus.OK, found.status());
//         Application a = found.application();

//         assertEquals(created.getId(), a.getId());
//         assertEquals("Тема", a.getTitle());
//         assertEquals("Текст тезиса", a.getThesisText());
//         assertEquals("Иванов Иван Иванович", a.getAuthorFullName());
//         assertEquals("ПрИн-467", a.getAuthorGroup());
//         assertEquals("abc123@yandex.ru", a.getAuthorEmail());
//         assertEquals("88000000000", a.getAuthorPhone());
//         assertEquals(ApplicationStatus.WITHDRAWN, a.getStatus());

//         ServiceResult result = service.edit(created.getId(), request("Другая"));

//         assertEquals(ServiceStatus.WITHDRAWN, result.status());
//         assertNull(result.application());
//         assertEquals(1, count(service));
//     }

//     @Test
//     void test_edit_afterDeadline() {
//         ApplicationService before = serviceAt(BEFORE);
//         Application created = create(before, "Тема");

//         ApplicationService after = serviceAt(AFTER);
//         ServiceResult result = after.edit(created.getId(), request("Новая"));

//         assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
//         assertNull(result.application());

//         // Заявка не изменилась
//         ServiceResult stored = after.get(created.getId());
//         assertEquals("Тема", stored.application().getTitle());
//         assertEquals(1, count(after));

//         assertEquals("Тема", stored.application().getTitle());
//         assertEquals("Текст тезиса", stored.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", stored.application().getAuthorFullName());
//         assertEquals("ПрИн-467", stored.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", stored.application().getAuthorEmail());
//         assertEquals("88000000000", stored.application().getAuthorPhone());
//     }

//     // ---------------- PATCH ----------------

//     @Test
//     void test_withdraw_changesStatusToWithdrawn() {
//         ApplicationService service = serviceAt(BEFORE);
//         Application created = create(service, "Тема");

//         ServiceResult result = service.withdraw(created.getId());

//         assertEquals(ServiceStatus.OK, result.status());
//         assertNotNull(result.application());
//         assertEquals(ApplicationStatus.WITHDRAWN, result.application().getStatus());

//         assertEquals("Текст тезиса", result.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", result.application().getAuthorFullName());
//         assertEquals("ПрИн-467", result.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", result.application().getAuthorEmail());
//         assertEquals("88000000000", result.application().getAuthorPhone());
//         assertEquals(1, count(service));
//     }

//     @Test
//     void test_withdraw_unknownId() {
//         ApplicationService service = serviceAt(BEFORE);

//         ServiceResult result = service.withdraw("0101010101");

//         assertEquals(ServiceStatus.NOT_FOUND, result.status());
//         assertNull(result.application());
//         assertEquals(0, count(service));
//     }

//     @Test
//     void test_withdraw_doubleTimes() {
//         ApplicationService service = serviceAt(BEFORE);
//         Application created = create(service, "Тема");

//         ServiceResult result = service.withdraw(created.getId());

//         assertEquals(ServiceStatus.OK, result.status());
//         assertNotNull(result.application());
//         assertEquals(ApplicationStatus.WITHDRAWN, result.application().getStatus());

//         ServiceResult result2 = service.withdraw(created.getId());

//         assertEquals(ServiceStatus.OK, result2.status());
//         assertNotNull(result2.application());
//         assertEquals(ApplicationStatus.WITHDRAWN, result2.application().getStatus());

//         assertEquals("Текст тезиса", result2.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", result2.application().getAuthorFullName());
//         assertEquals("ПрИн-467", result2.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", result2.application().getAuthorEmail());
//         assertEquals("88000000000", result2.application().getAuthorPhone());
//         assertEquals(1, count(service));
//     }

//     @Test
//     void test_withdraw_afterDeadline() {
//         ApplicationService before = serviceAt(BEFORE);
//         Application created = create(before, "Тема");

//         ApplicationService after = serviceAt(AFTER);
//         ServiceResult result = after.withdraw(created.getId());

//         assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
//         assertNull(result.application());

//         ServiceResult stored = after.get(created.getId());
//         assertEquals(ApplicationStatus.SUBMITTED, stored.application().getStatus());
//         assertEquals(1, count(after));

//         assertEquals("Текст тезиса", stored.application().getThesisText());
//         assertEquals("Иванов Иван Иванович", stored.application().getAuthorFullName());
//         assertEquals("ПрИн-467", stored.application().getAuthorGroup());
//         assertEquals("abc123@yandex.ru", stored.application().getAuthorEmail());
//         assertEquals("88000000000", stored.application().getAuthorPhone());
//     }
// }
