package com.kulikov.conference_app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kulikov.conference_app.auth.CurrentUser;
import com.kulikov.conference_app.config.ConferenceProperties;
import com.kulikov.conference_app.model.ApplicantCategory;
import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationAuthor;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.model.CoAuthorRequest;
import com.kulikov.conference_app.model.FundingType;
import com.kulikov.conference_app.model.Organizer;
import com.kulikov.conference_app.model.Participant;
import com.kulikov.conference_app.model.ParticipantRequest;
import com.kulikov.conference_app.model.StaffRequest;
import com.kulikov.conference_app.model.SupervisorRequest;
import com.kulikov.conference_app.repositories.InMemoryApplicationRepository;
import com.kulikov.conference_app.repositories.InMemoryDirectionRepository;
import com.kulikov.conference_app.repositories.InMemoryPersonRepository;

/**
 * Тесты соавторов
 */
public class ApplicationAuthorsTests {

    private static final Instant DEADLINE = Instant.parse("2026-03-15T23:59:59Z");
    private static final Instant DEADLINE_T2 = Instant.parse("2026-03-20T23:59:59Z");
    private static final String BEFORE = "2026-03-10T12:00:00Z";

    private static final List<SupervisorRequest> SUPERVISORS = List.of(
            new SupervisorRequest("Дроботов Алексей Владимирович", "ВолгГТУ, каф. АПП"));

    private final InMemoryApplicationRepository _appRepo = new InMemoryApplicationRepository();
    private final InMemoryPersonRepository _personRepo = new InMemoryPersonRepository();
    private final InMemoryDirectionRepository _dirRepo = new InMemoryDirectionRepository();

    private ApplicationService serviceAt(String now, String personId) {
        ConferenceProperties props = new ConferenceProperties();
        props.setDeadlineT1(DEADLINE);
        props.setDeadlineT2(DEADLINE_T2);
        Clock clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC);
        CurrentUser currentUser = new CurrentUser() {
            @Override
            public Optional<String> personId() {
                return personId == null ? Optional.empty() : Optional.of(personId);
            }
        };
        return new ApplicationService(_appRepo, _personRepo, _dirRepo, currentUser, props, clock);
    }

    private Participant participant(String fullName, String email) {
        ParticipantRequest req = new ParticipantRequest(fullName, email, "88000000000", "password123");
        return (Participant)_personRepo.save(new Participant(req, "hash"));
    }

    private Organizer organizer(int directionId) {
        StaffRequest req = new StaffRequest("Сидоров Сидор", "org" + directionId + "@yandex.ru",
                "password123", directionId);
        return (Organizer)_personRepo.save(new Organizer(req, "hash", _dirRepo.findById(directionId).orElseThrow()));
    }

    private CoAuthorRequest coAuthor(String email) {
        return new CoAuthorRequest(email, "ПрИн-468", FundingType.CONTRACT);
    }

    private ApplicationRequest request(String title, int directionId, CoAuthorRequest coAuthor) {
        return new ApplicationRequest(title, "Аннотация", "Текст тезиса", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, directionId, SUPERVISORS, coAuthor);
    }

    // ---------------- Подача с соавтором ----------------

    @Test
    void test_submit_withCoAuthorAndSupervisors() {
        Participant ivan = participant("Иванов Иван Иванович", "ivan@yandex.ru");
        Participant petr = participant("Петров Пётр Петрович", "petr@yandex.ru");

        ServiceResult result = serviceAt(BEFORE, ivan.getId()).submit(request("Тема", 7, coAuthor("petr@yandex.ru")));

        assertEquals(ServiceStatus.CREATED, result.status());
        Application a = result.application();
        assertEquals(2, a.getAuthors().size());

        ApplicationAuthor first = a.getAuthors().get(0);
        assertEquals(ivan.getId(), first.getPerson().getId());
        assertEquals(ivan.getId(), a.getOwner().getId());
        assertEquals("ПрИн-467", first.getGroupName());
        assertEquals(FundingType.BUDGET, first.getFundingType());

        ApplicationAuthor second = a.getAuthors().get(1);
        assertEquals(petr.getId(), second.getPerson().getId());
        assertEquals("Петров Пётр Петрович", second.getPerson().getFullName());
        assertEquals("ПрИн-468", second.getGroupName());
        assertEquals(FundingType.CONTRACT, second.getFundingType());

        assertEquals(1, a.getSupervisors().size());
        assertEquals("Дроботов Алексей Владимирович", a.getSupervisors().get(0).getFullName());
        assertEquals("ВолгГТУ, каф. АПП", a.getSupervisors().get(0).getWorkplace());
    }

    @Test
    void test_submit_invalidCoAuthor() {
        Participant ivan = participant("Иванов Иван Иванович", "ivan@yandex.ru");
        organizer(7);
        ApplicationService service = serviceAt(BEFORE, ivan.getId());

        ServiceResult unknown = service.submit(request("Тема", 7, coAuthor("nobody@yandex.ru")));
        ServiceResult himself = service.submit(request("Тема", 7, coAuthor("IVAN@yandex.ru")));
        ServiceResult notParticipant = service.submit(request("Тема", 7, coAuthor("org7@yandex.ru")));

        assertEquals(ServiceStatus.INVALID_COAUTHOR, unknown.status());
        assertNull(unknown.application());
        assertEquals(ServiceStatus.INVALID_COAUTHOR, himself.status());
        assertNull(himself.application());
        assertEquals(ServiceStatus.INVALID_COAUTHOR, notParticipant.status());
        assertNull(notParticipant.application());
        assertEquals(0, _appRepo.findAll().size());
    }

    @Test
    void test_coAuthor_canEdit() {
        Participant ivan = participant("Иванов Иван Иванович", "ivan@yandex.ru");
        Participant petr = participant("Петров Пётр Петрович", "petr@yandex.ru");
        Application created = serviceAt(BEFORE, ivan.getId())
                .submit(request("Тема", 7, coAuthor("petr@yandex.ru"))).application();
        ApplicationService petrService = serviceAt(BEFORE, petr.getId());

        // видит
        ServiceListResult list = petrService.findAll();
        assertEquals(1, list.applications().size());
        assertEquals(created.getId(), list.applications().get(0).getId());
        assertEquals(ServiceStatus.OK, petrService.get(created.getId()).status());

        // правит заявку
        CoAuthorRequest newData = new CoAuthorRequest("petr@yandex.ru", "ПрИн-466", FundingType.BUDGET);
        ServiceResult edited = petrService.edit(created.getId(), request("Новая тема", 7, newData));
        assertEquals(ServiceStatus.OK, edited.status());
        assertEquals("Новая тема", edited.application().getTitle());
        assertEquals("ПрИн-466", edited.application().getAuthors().get(1).getGroupName());
        assertEquals(FundingType.BUDGET, edited.application().getAuthors().get(1).getFundingType());
    }

    @Test
    void test_transfer_limitCountsCoAuthor() {
        Participant ivan = participant("Иванов Иван Иванович", "ivan@yandex.ru");
        Participant petr = participant("Петров Пётр Петрович", "petr@yandex.ru");
        Organizer organizer7 = organizer(7);
        ApplicationService petrService = serviceAt(BEFORE, petr.getId());
        petrService.submit(request("Своя 1", 8, null));
        petrService.submit(request("Своя 2", 8, null));
        Application moving = serviceAt(BEFORE, ivan.getId())
                .submit(request("Совместная", 7, coAuthor("petr@yandex.ru"))).application();

        ServiceResult result = serviceAt(BEFORE, organizer7.getId()).transfer(moving.getId(), 8);

        assertEquals(ServiceStatus.LIMIT_EXCEEDED, result.status());
        assertNull(result.application());
        assertEquals(7, moving.getDirection().getId());
    }
}
