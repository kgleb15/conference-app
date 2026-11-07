package com.kulikov.conference_app.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.repositories.InMemoryPersonRepository;

public class PersonServiceTests {

    private final InMemoryPersonRepository _repo = new InMemoryPersonRepository();
    private final BCryptPasswordEncoder _encoder = new BCryptPasswordEncoder();
    private final PersonService _service = new PersonService(_repo, _encoder);

    private PersonRequest request(String fullName) {
        return new PersonRequest(fullName, "abc123@yandex.ru", "88000000000", "password123");
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
        assertEquals("88000000000", p.getPhone());
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
        PersonResult result = _service.registerOrganizer(request("Сидоров Сидор Сидорович"));

        assertEquals(ServiceStatus.CREATED, result.status());
        Person p = result.person();
        assertNotNull(p);
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Сидоров Сидор Сидорович", p.getFullName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals("88000000000", p.getPhone());
        assertEquals(PersonRole.ORGANIZER, p.getRole());
        assertTrue(_encoder.matches("password123", p.getPasswordHash()));

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertEquals(PersonRole.ORGANIZER, _repo.findById(p.getId()).get().getRole());
    }

    @Test
    void test_registerExpert_createsExpert() {
        PersonResult result = _service.registerExpert(request("Петров Пётр Петрович"));

        assertEquals(ServiceStatus.CREATED, result.status());
        Person p = result.person();
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Петров Пётр Петрович", p.getFullName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals("88000000000", p.getPhone());
        assertEquals(PersonRole.EXPERT, p.getRole());

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertTrue(_encoder.matches("password123", p.getPasswordHash()));
        assertEquals(PersonRole.EXPERT, _repo.findById(p.getId()).get().getRole());
    }

    @Test
    void test_register_duplicateEmail() {
        Person first = _service.register(request("Иванов Иван Иванович")).person();

        PersonRequest duplicate = new PersonRequest("Петров Пётр Петрович",
                "ABC123@Yandex.ru", "89990000000", "password456");
        PersonResult asParticipant = _service.register(duplicate);
        PersonResult asOrganizer = _service.registerOrganizer(duplicate);
        PersonResult asExpert = _service.registerExpert(duplicate);

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
