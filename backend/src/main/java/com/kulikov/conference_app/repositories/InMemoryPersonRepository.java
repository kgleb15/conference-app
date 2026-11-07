package com.kulikov.conference_app.repositories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Person;

/**
 * Реализация PersonRepository через хранение в оперативной памяти
 */
@Repository
@Profile("!db")
public class InMemoryPersonRepository implements PersonRepository {

    private final Map<String, Person> _storage = new LinkedHashMap<>();

    @Override
    public Optional<Person> findById(String id) {
        return Optional.ofNullable(_storage.get(id));
    }

    @Override
    public Optional<Person> findByEmail(String email) {
        for (Person person : _storage.values()) {
            if (person.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(person);
            }
        }
        return Optional.empty();
    }

    @Override
    public Person save(Person person) {
        _storage.put(person.getId(), person);
        return person;
    }
}
