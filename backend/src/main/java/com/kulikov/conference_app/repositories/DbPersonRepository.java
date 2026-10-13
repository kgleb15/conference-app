package com.kulikov.conference_app.repositories;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Person;

/**
 * Реализация PersonRepository через Spring Data JPA
 */
@Repository
@Profile("db")
public class DbPersonRepository implements PersonRepository {

    private final PersonJpaRepository _jpa;

    public DbPersonRepository(PersonJpaRepository jpa) {
        this._jpa = jpa;
    }

    @Override
    public Optional<Person> findById(String id) {
        return _jpa.findById(id);
    }

    @Override
    public Person save(Person person) {
        return _jpa.save(person);
    }
}
