package com.kulikov.conference_app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kulikov.conference_app.model.Person;

public interface PersonJpaRepository extends JpaRepository<Person, String> {

}
