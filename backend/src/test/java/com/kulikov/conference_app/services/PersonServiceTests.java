package com.kulikov.conference_app.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.repositories.InMemoryPersonRepository;

public class PersonServiceTests {

    private final InMemoryPersonRepository _repo = new InMemoryPersonRepository();
    private final PersonService _service = new PersonService(_repo);

    private PersonRequest request(String fullName) {
        return new PersonRequest(fullName, "ПрИн-467", "abc123@yandex.ru", "88000000000");
    }

    @Test
    void test_register_createsParticipant() {
        Person p = _service.register(request("Иванов Иван Иванович"));

        assertNotNull(p);
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Иванов Иван Иванович", p.getFullName());
        assertEquals("ПрИн-467", p.getGroupName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals("88000000000", p.getPhone());
        assertEquals(PersonRole.PARTICIPANT, p.getRole());

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertEquals(p, _repo.findById(p.getId()).get());
    }

    @Test
    void test_registerOrganizer_createsOrganizer() {
        Person p = _service.registerOrganizer(request("Сидоров Сидор Сидорович"));

        assertNotNull(p);
        assertNotNull(p.getId());
        assertFalse(p.getId().isBlank());
        assertEquals("Сидоров Сидор Сидорович", p.getFullName());
        assertEquals("ПрИн-467", p.getGroupName());
        assertEquals("abc123@yandex.ru", p.getEmail());
        assertEquals("88000000000", p.getPhone());
        assertEquals(PersonRole.ORGANIZER, p.getRole());

        assertTrue(_repo.findById(p.getId()).isPresent());
        assertEquals(PersonRole.ORGANIZER, _repo.findById(p.getId()).get().getRole());
    }
}