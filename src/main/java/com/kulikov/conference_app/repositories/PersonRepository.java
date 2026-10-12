package com.kulikov.conference_app.repositories;

import java.util.Optional;

import com.kulikov.conference_app.model.Person;

public interface PersonRepository {

    /**
     * Возвращает человека по id
     *
     * @param id - идентификатор человека
     * @return найденный человек или empty, если не найден
     */
    Optional<Person> findById(String id);

    /**
     * Сохраняет человека
     *
     * @param person - человек для сохранения
     * @return сохранённый человек
     */
    Person save(Person person);
}
