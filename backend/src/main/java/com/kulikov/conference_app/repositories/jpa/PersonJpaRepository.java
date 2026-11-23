package com.kulikov.conference_app.repositories.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kulikov.conference_app.model.person.Person;

public interface PersonJpaRepository extends JpaRepository<Person, String> {

    Optional<Person> findByEmailIgnoreCase(String email);
}
