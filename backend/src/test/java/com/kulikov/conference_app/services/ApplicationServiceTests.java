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

    private static final Instant DEADLINE_T2 = Instant.parse("2026-03-20T23:59:59Z");
    private static final String AFTER_T2 = "2026-03-21T00:00:00Z";

    private final InMemoryApplicationRepository _appRepo = new InMemoryApplicationRepository();
    private final InMemoryPersonRepository _personRepo = new InMemoryPersonRepository();

    private ApplicationService serviceAt(String now, String personId) {
        ConferenceProperties props = new ConferenceProperties();
        props.setDeadlineT1(DEADLINE);
        props.setDeadlineT2(DEADLINE_T2);
        Clock clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC);
        CurrentUser currentUser = new CurrentUser() {
            @Override
            public Optional<String> personId() {
                if (personId == null || personId.isBlank()) {
                    return Optional.empty();
                }
                return Optional.of(personId);
            }
        };
        return new ApplicationService(_appRepo, _personRepo, currentUser, props, clock);
    }

    /** Регистрирует участника и возвращает его сущность */
    private Person registerPerson(String fullName) {
        PersonRequest req = new PersonRequest(fullName, "ПрИн-467",
                "abc123@yandex.ru", "88000000000");
        return _personRepo.save(new Person(req, PersonRole.PARTICIPANT));
    }

    /** Регистрирует организатора */
    private Person registerOrganizer(String fullName) {
        PersonRequest req = new PersonRequest(fullName, "org",
                "org@yandex.ru", "80000000000");
        return _personRepo.save(new Person(req, PersonRole.ORGANIZER));
    }

    /** Создаёт запрос заявки с заданным заголовком */
    private ApplicationRequest request(String title) {
        return new ApplicationRequest(title, "Текст тезиса");
    }

    private Application create(ApplicationService service, String title) {
        return service.submit(request(title)).application();
    }

    /**
     * Считает количество заявок у текущего пользователя
     */
    private int count(ApplicationService service) {
        ServiceListResult result = service.findAll();
        if (result.applications() == null) {
            return 0;
        }
        int n = 0;
        for (Application a : result.applications()) {
            n++;
        }
        return n;
    }

    /** Переводит заявку в REVISION_REQUESTED, как это сделает организатор */
    private void requestRevision(Application a, Person organizer) {
        a.requestRevision(organizer, "Допишите введение", Instant.parse(BEFORE));
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
    void test_findAll_showsOnlyOwnApplications() {
        Person first = registerPerson("Иванов Иван Иванович");
        Person second = registerPerson("Петров Пётр Петрович");

        ApplicationService firstService = serviceAt(BEFORE, first.getId());
        ApplicationService secondService = serviceAt(BEFORE, second.getId());

        create(firstService, "Первая");
        create(firstService, "Вторая");
        create(secondService, "Третья");

        assertEquals(2, count(firstService));
        assertEquals(1, count(secondService));

        ServiceListResult firstList = firstService.findAll();
        assertEquals(ServiceStatus.OK, firstList.status());
        for (Application a : firstList.applications()) {
            assertEquals(first.getId(), a.getOwner().getId());
        }
    }

    @Test
    void test_findAll_noCurrentUser() {
        ApplicationService service = serviceAt(BEFORE, null);

        ServiceListResult result = service.findAll();

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.applications());
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

    @Test
    void test_get_otherPersonsApplication() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person other = registerPerson("Петров Пётр Петрович");

        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ApplicationService otherService = serviceAt(BEFORE, other.getId());
        ServiceResult result = otherService.get(created.getId());

        assertEquals(ServiceStatus.NOT_FOUND, result.status());
        assertNull(result.application());
    }

    @Test
    void test_get_noCurrentUser() {
        ApplicationService service = serviceAt(BEFORE, null);

        ServiceResult result = service.get("1234567890");

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.application());
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

    @Test
    void test_edit_otherPersonsApplication() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person other = registerPerson("Петров Пётр Петрович");

        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ApplicationService otherService = serviceAt(BEFORE, other.getId());
        ServiceResult result = otherService.edit(created.getId(), request("Взлом"));

        assertEquals(ServiceStatus.NOT_FOUND, result.status());
        assertNull(result.application());

        // Заявка не изменилась
        ServiceResult stored = ownerService.get(created.getId());
        assertEquals("Тема", stored.application().getTitle());
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

    @Test
    void test_withdraw_otherPersonsApplication_returnsNotFound() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person other = registerPerson("Петров Пётр Петрович");

        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ApplicationService otherService = serviceAt(BEFORE, other.getId());
        ServiceResult result = otherService.withdraw(created.getId());

        assertEquals(ServiceStatus.NOT_FOUND, result.status());
        assertNull(result.application());

        // Статус не изменился
        ServiceResult stored = ownerService.get(created.getId());
        assertEquals(ApplicationStatus.SUBMITTED, stored.application().getStatus());
    }

    @Test
    void test_submit_organizerForbidden() {
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService service = serviceAt(BEFORE, organizer.getId());

        ServiceResult result = service.submit(request("Тема"));

        assertEquals(ServiceStatus.FORBIDDEN, result.status());
        assertNull(result.application());
        assertEquals(0, count(service));
    }

    @Test
    void test_edit_organizerForbidden() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ServiceResult result = serviceAt(BEFORE, organizer.getId()).edit(created.getId(), request("12345"));

        assertEquals(ServiceStatus.FORBIDDEN, result.status());
        assertNull(result.application());

        Application stored = ownerService.get(created.getId()).application();
        assertEquals("Тема", stored.getTitle());
        assertEquals("Текст тезиса", stored.getThesisText());
        assertEquals(ApplicationStatus.SUBMITTED, stored.getStatus());
        assertEquals(0, stored.getEditCount());
        assertNull(stored.getLastEditedAt());
        assertNull(stored.getWithdrawnAt());
        assertEquals(owner.getId(), stored.getOwner().getId());
        assertEquals(1, count(ownerService));
    }

    @Test
    void test_withdraw_organizerForbidden() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ServiceResult result = serviceAt(BEFORE, organizer.getId()).withdraw(created.getId());

        assertEquals(ServiceStatus.FORBIDDEN, result.status());
        assertNull(result.application());

        Application stored = ownerService.get(created.getId()).application();
        assertEquals(ApplicationStatus.SUBMITTED, stored.getStatus());
        assertEquals("Тема", stored.getTitle());
        assertEquals(0, stored.getEditCount());
        assertNull(stored.getWithdrawnAt());
        assertEquals(owner.getId(), stored.getOwner().getId());
        assertEquals(1, count(ownerService));
    }

    @Test
    void test_findAllForOrganizer_returnsAllApplications() {
        Person first = registerPerson("Иванов Иван Иванович");
        Person second = registerPerson("Петров Пётр Петрович");
        Person organizer = registerOrganizer("Сидоров Сидор");

        ApplicationService firstService = serviceAt(BEFORE, first.getId());
        ApplicationService secondService = serviceAt(BEFORE, second.getId());
        ApplicationService orgService = serviceAt(BEFORE, organizer.getId());

        create(firstService, "Первая");
        create(firstService, "Вторая");
        create(secondService, "Третья");

        ServiceListResult result = orgService.findAllForOrganizer(null, null);

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.applications());
        assertEquals(3, result.applications().size());
    }

    @Test
    void test_findAllForOrganizer_noCurrentUser() {
        ServiceListResult result = serviceAt(BEFORE, null).findAllForOrganizer(null, null);

        assertEquals(ServiceStatus.UNAUTHORIZED, result.status());
        assertNull(result.applications());
    }

    @Test
    void test_findAllForOrganizer_participantForbidden() {
        Person participant = registerPerson("Иванов Иван Иванович");
        create(serviceAt(BEFORE, participant.getId()), "Первая");

        ServiceListResult result = serviceAt(BEFORE, participant.getId()).findAllForOrganizer(null, null);

        assertEquals(ServiceStatus.FORBIDDEN, result.status());
        assertNull(result.applications());
        assertNotNull(participant);
    }

    @Test
    void test_findAllForOrganizer_filterByStatus() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");

        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        ApplicationService orgService = serviceAt(BEFORE, organizer.getId());

        create(ownerService, "Первая");
        Application second = create(ownerService, "Вторая");
        ownerService.withdraw(second.getId());

        ServiceListResult submitted = orgService.findAllForOrganizer(ApplicationStatus.SUBMITTED, null);

        assertEquals(ServiceStatus.OK, submitted.status());
        assertEquals(1, submitted.applications().size());
        assertEquals("Первая", submitted.applications().get(0).getTitle());

        ServiceListResult withdrawn = orgService.findAllForOrganizer(ApplicationStatus.WITHDRAWN, null);
        assertEquals(ServiceStatus.OK, withdrawn.status());
        assertEquals(1, withdrawn.applications().size());
        assertEquals("Вторая", withdrawn.applications().get(0).getTitle());
    }

    @Test
    void test_findAllForOrganizer_filterByEdited() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");

        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        ApplicationService orgService = serviceAt(BEFORE, organizer.getId());

        create(ownerService, "Не изменённая");
        Application edited = create(ownerService, "Изменённая");
        ownerService.edit(edited.getId(), request("После правки"));

        ServiceListResult onlyEdited = orgService.findAllForOrganizer(null, true);
        assertEquals(ServiceStatus.OK, onlyEdited.status());
        assertEquals(1, onlyEdited.applications().size());
        assertEquals("После правки", onlyEdited.applications().get(0).getTitle());

        ServiceListResult onlyNotEdited = orgService.findAllForOrganizer(null, false);
        assertEquals(ServiceStatus.OK, onlyNotEdited.status());
        assertEquals(1, onlyNotEdited.applications().size());
        assertEquals("Не изменённая", onlyNotEdited.applications().get(0).getTitle());
    }

    @Test
    void test_edit_revisionRequestedAfterT1BeforeT2() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");
        requestRevision(created, organizer);

        ServiceResult result = serviceAt(AFTER, owner.getId()).edit(created.getId(), request("Доработанная"));

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        assertEquals(created.getId(), result.application().getId());
        assertEquals("Доработанная", result.application().getTitle());
        assertEquals("Текст тезиса", result.application().getThesisText());
        assertEquals(ApplicationStatus.SUBMITTED, result.application().getStatus());
        assertEquals(1, result.application().getEditCount());
        assertNotNull(result.application().getLastEditedAt());
        assertNull(result.application().getWithdrawnAt());
        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals(organizer.getId(), result.application().getDecidedBy().getId());
        assertEquals("Допишите введение", result.application().getModerationComment());
        assertEquals(1, count(serviceAt(AFTER, owner.getId())));
    }

    @Test
    void test_edit_revisionRequestedAfterT2() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");
        requestRevision(created, organizer);

        ApplicationService late = serviceAt(AFTER_T2, owner.getId());
        ServiceResult result = late.edit(created.getId(), request("Слишком поздно"));

        assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
        assertNull(result.application());
        Application stored = late.get(created.getId()).application();
        assertEquals("Тема", stored.getTitle());
        assertEquals(ApplicationStatus.REVISION_REQUESTED, stored.getStatus());
        assertEquals(0, stored.getEditCount());
        assertNull(stored.getLastEditedAt());
    }

    @Test
    void test_edit_approvedAndRejectedInvalidState() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application approved = create(service, "Утверждённая");
        Application rejected = create(service, "Отклонённая");
        approved.approve(organizer, null, Instant.parse(BEFORE));
        rejected.reject(organizer, "Нет", Instant.parse(BEFORE));

        ServiceResult first = service.edit(approved.getId(), request("Новая"));
        ServiceResult second = service.edit(rejected.getId(), request("Новая"));

        assertEquals(ServiceStatus.INVALID_STATE, first.status());
        assertNull(first.application());
        assertEquals(ServiceStatus.INVALID_STATE, second.status());
        assertNull(second.application());

        assertEquals("Утверждённая", service.get(approved.getId()).application().getTitle());
        assertEquals(ApplicationStatus.APPROVED, service.get(approved.getId()).application().getStatus());
        assertEquals("Отклонённая", service.get(rejected.getId()).application().getTitle());
        assertEquals(ApplicationStatus.REJECTED, service.get(rejected.getId()).application().getStatus());
        assertEquals(0, service.get(approved.getId()).application().getEditCount());
        assertEquals(0, service.get(rejected.getId()).application().getEditCount());
    }

    @Test
    void test_withdraw_revisionRequestedAfterT1() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");
        requestRevision(created, organizer);

        assertEquals(ApplicationStatus.REVISION_REQUESTED, created.getStatus());
        ServiceResult result = serviceAt(AFTER, owner.getId()).withdraw(created.getId());

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        assertEquals(ApplicationStatus.WITHDRAWN, result.application().getStatus());
        assertNotNull(result.application().getWithdrawnAt());
        assertEquals("Тема", result.application().getTitle());
        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals("Допишите введение", result.application().getModerationComment());
    }

    @Test
    void test_withdraw_revisionRequestedAfterT2() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");
        requestRevision(created, organizer);

        ApplicationService late = serviceAt(AFTER_T2, owner.getId());
        ServiceResult result = late.withdraw(created.getId());

        assertEquals(ServiceStatus.DEADLINE_PASSED, result.status());
        assertNull(result.application());
        assertEquals(ApplicationStatus.REVISION_REQUESTED, late.get(created.getId()).application().getStatus());
        assertNull(late.get(created.getId()).application().getWithdrawnAt());
    }

    @Test
    void test_withdraw_alreadyApproved() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService service = serviceAt(BEFORE, owner.getId());
        Application created = create(service, "Тема");
        created.approve(organizer, "Принята", Instant.parse(BEFORE));

        ServiceResult result = service.withdraw(created.getId());

        assertEquals(ServiceStatus.INVALID_STATE, result.status());
        assertNull(result.application());

        Application stored = service.get(created.getId()).application();
        assertEquals(ApplicationStatus.APPROVED, stored.getStatus());
        assertNull(stored.getWithdrawnAt());
        assertEquals("Принята", stored.getModerationComment());
        assertEquals(1, count(service));
    }

    @Test
    void test_approve_byOrganizer() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ServiceResult result = serviceAt(BEFORE, organizer.getId()).approve(created.getId(), "Принята");

        assertEquals(ServiceStatus.OK, result.status());
        assertNotNull(result.application());
        Application a = result.application();
        assertEquals(created.getId(), a.getId());
        assertEquals(ApplicationStatus.APPROVED, a.getStatus());
        assertEquals("Принята", a.getModerationComment());
        assertEquals(Instant.parse(BEFORE), a.getDecidedAt());
        assertEquals(Instant.parse(BEFORE), a.getUpdatedAt());
        assertNotNull(a.getDecidedBy());
        assertEquals(organizer.getId(), a.getDecidedBy().getId());
        assertEquals(PersonRole.ORGANIZER, a.getDecidedBy().getRole());
        assertEquals("Тема", a.getTitle());
        assertEquals("Текст тезиса", a.getThesisText());
        assertEquals(owner.getId(), a.getOwner().getId());
        assertEquals(0, a.getEditCount());
        assertNull(a.getWithdrawnAt());
    }

    @Test
    void test_reject_byOrganizer() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");

        ServiceResult result = serviceAt(AFTER_T2, organizer.getId()).reject(created.getId(), "Не по теме");

        assertEquals(ServiceStatus.OK, result.status());
        assertEquals(ApplicationStatus.REJECTED, result.application().getStatus());
        assertEquals("Не по теме", result.application().getModerationComment());
        assertEquals(Instant.parse(AFTER_T2), result.application().getDecidedAt());
        assertEquals(organizer.getId(), result.application().getDecidedBy().getId());
        assertEquals(owner.getId(), result.application().getOwner().getId());
        assertEquals(0, result.application().getEditCount());
    }

    @Test
    void test_requestRevision_byOrganizer() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Person organizer = registerOrganizer("Сидоров Сидор");
        ApplicationService ownerService = serviceAt(BEFORE, owner.getId());
        Application created = create(ownerService, "Тема");

        ServiceResult result = serviceAt(BEFORE, organizer.getId()).requestRevision(created.getId(), "Допишите");

        assertEquals(ServiceStatus.OK, result.status());
        assertEquals(ApplicationStatus.REVISION_REQUESTED, result.application().getStatus());
        assertEquals("Допишите", result.application().getModerationComment());
        assertEquals(Instant.parse(BEFORE), result.application().getDecidedAt());
        assertEquals(organizer.getId(), result.application().getDecidedBy().getId());

        // участник после T1, но до T2 дорабатывает заявку
        ServiceResult edited = serviceAt(AFTER, owner.getId()).edit(created.getId(), request("Доработанная"));
        assertEquals(ServiceStatus.OK, edited.status());
        assertEquals(ApplicationStatus.SUBMITTED, edited.application().getStatus());
        assertEquals(1, edited.application().getEditCount());
    }

    @Test
    void test_moderation_noCurrentUserOrParticipant() {
        Person owner = registerPerson("Иванов Иван Иванович");
        Application created = create(serviceAt(BEFORE, owner.getId()), "Тема");

        ApplicationService anonymous = serviceAt(BEFORE, null);
        assertEquals(ServiceStatus.UNAUTHORIZED, anonymous.approve(created.getId(), "ok").status());
        assertEquals(ServiceStatus.UNAUTHORIZED, anonymous.reject(created.getId(), "no").status());
        assertEquals(ServiceStatus.UNAUTHORIZED, anonymous.requestRevision(created.getId(), "fix").status());

        ApplicationService participant = serviceAt(BEFORE, owner.getId());
        ServiceResult forbidden = participant.approve(created.getId(), "ok");
        assertEquals(ServiceStatus.FORBIDDEN, forbidden.status());
        assertNull(forbidden.application());
        assertEquals(ServiceStatus.FORBIDDEN, participant.reject(created.getId(), "no").status());
        assertEquals(ServiceStatus.FORBIDDEN, participant.requestRevision(created.getId(), "fix").status());

        assertEquals(ApplicationStatus.SUBMITTED, created.getStatus());
        assertNull(created.getDecidedBy());
        assertNull(created.getDecidedAt());
        assertNull(created.getModerationComment());
    }
}
