package com.kulikov.conference_app.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kulikov.conference_app.model.Participant;
import com.kulikov.conference_app.model.ParticipantRequest;
import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.model.Staff;
import com.kulikov.conference_app.model.StaffRequest;
import com.kulikov.conference_app.repositories.InMemoryDirectionRepository;
import com.kulikov.conference_app.repositories.InMemoryPersonRepository;

public class PersonServiceTests {

    private final InMemoryPersonRepository _repo = new InMemoryPersonRepository();
    private final InMemoryDirectionRepository _directions = new InMemoryDirectionRepository();
    private final BCryptPasswordEncoder _encoder = new BCryptPasswordEncoder();
    private final PersonService _service = new PersonService(_repo, _encoder, _directions);

    private ParticipantRequest request(String fullName) {
        return new ParticipantRequest(fullName, "abc123@yandex.ru", "88000000000", "password123");
    }

    private StaffRequest staffRequest(String fullName, int directionId) {
        return new StaffRequest(fullName, "abc123@yandex.ru", "password123", directionId);
    }

    @Test
    void test_register_createsParticipant() {
        PersonResult result = _service.register(request("Иванов Иван Иванович"));

        assertEquals(ServiceStatus.CREATED, result.status());
        Person p = result.person();
        assertNotNull(p);
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Иванов Иван Иванович", p.getFullName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertInstanceOf(Participant.class, p);
        assertEquals("88000000000", ((Participant) p).getPhone());
        assertEquals(PersonRole.PARTICIPANT, p.getRole());

        // пароль хранится только в виде хеша
        assertNotNull(p.getPasswordHash());
        assertNotEquals("password123", p.getPasswordHash());
        assertTrue(_encoder.matches("password123", p.getPasswordHash()));
        assertFalse(_encoder.matches("wrong-password", p.getPasswordHash()));

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertEquals(p, _repo.findById(p.getId()).get());
    }

    @Test
    void test_registerOrganizer_createsOrganizer() {
        PersonResult result = _service.registerOrganizer(staffRequest("Сидоров Сидор Сидорович", 7));

        assertEquals(ServiceStatus.CREATED, result.status());
        Person p = result.person();
        assertNotNull(p);
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Сидоров Сидор Сидорович", p.getFullName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals(PersonRole.ORGANIZER, p.getRole());
        assertInstanceOf(Staff.class, p);
        assertEquals(7, ((Staff) p).getDirection().getId());
        assertTrue(_encoder.matches("password123", p.getPasswordHash()));

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertEquals(PersonRole.ORGANIZER, _repo.findById(p.getId()).get().getRole());
    }

    @Test
    void test_registerExpert_createsExpert() {
        PersonResult result = _service.registerExpert(staffRequest("Петров Пётр Петрович", 3));

        assertEquals(ServiceStatus.CREATED, result.status());
        Person p = result.person();
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Петров Пётр Петрович", p.getFullName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals(PersonRole.EXPERT, p.getRole());
        assertInstanceOf(Staff.class, p);
        assertEquals(3, ((Staff) p).getDirection().getId());

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertTrue(_encoder.matches("password123", p.getPasswordHash()));
        assertEquals(PersonRole.EXPERT, _repo.findById(p.getId()).get().getRole());
    }

    @Test
    void test_registerStaff_unknownDirection() {
        PersonResult organizer = _service.registerOrganizer(staffRequest("Сидоров Сидор Сидорович", 99));
        PersonResult expert = _service.registerExpert(staffRequest("Петров Пётр Петрович", 0));

        assertEquals(ServiceStatus.INVALID_DIRECTION, organizer.status());
        assertNull(organizer.person());
        assertEquals(ServiceStatus.INVALID_DIRECTION, expert.status());
        assertNull(expert.person());
        assertTrue(_repo.findByEmail("abc123@yandex.ru").isEmpty());
    }

    @Test
    void test_register_duplicateEmail() {
        Person first = _service.register(request("Иванов Иван Иванович")).person();

        ParticipantRequest duplicate = new ParticipantRequest("Петров Пётр Петрович",
                "ABC123@Yandex.ru", "89990000000", "password456");
        StaffRequest duplicateStaff = new StaffRequest("Петров Пётр Петрович",
                "ABC123@Yandex.ru", "password456", 7);
        PersonResult asParticipant = _service.register(duplicate);
        PersonResult asOrganizer = _service.registerOrganizer(duplicateStaff);
        PersonResult asExpert = _service.registerExpert(duplicateStaff);

        assertEquals(ServiceStatus.CONFLICT, asParticipant.status());
        assertNull(asParticipant.person());
        assertEquals(ServiceStatus.CONFLICT, asOrganizer.status());
        assertNull(asOrganizer.person());
        assertEquals(ServiceStatus.CONFLICT, asExpert.status());
        assertNull(asExpert.person());

        Person stored = _repo.findByEmail("abc123@yandex.ru").get();
        assertEquals(first.getId(), stored.getId());
        assertEquals("Иванов Иван Иванович", stored.getFullName());
        assertTrue(_encoder.matches("password123", stored.getPasswordHash()));
    }
}
