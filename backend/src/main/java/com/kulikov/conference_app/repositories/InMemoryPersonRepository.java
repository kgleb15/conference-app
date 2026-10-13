package com.kulikov.conference_app.repositories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Person;

/**
 * Реализация PersonRepository через хранение в оперативной памяти
 */
@Repository
@Primary
@Qualifier("IN_MEMORY")
public class InMemoryPersonRepository implements PersonRepository {

    private final Map<String, Person> _storage = new LinkedHashMap<>();

    @Override
    public Optional<Person> findById(String id) {
        return Optional.ofNullable(_storage.get(id));
    }

    @Override
    public Person save(Person person) {
        _storage.put(person.getId(), person);
        return person;
    }
}
