package com.kulikov.conference_app.services;

import org.springframework.stereotype.Service;

import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.repositories.PersonRepository;

/**
 * Сервис работы с людьми
 */
@Service
public class PersonService {

    private final PersonRepository _personRepository;

    public PersonService(PersonRepository personRepository) {
        this._personRepository = personRepository;
    }

    /**
     * Регистрирует нового участника конференции
     *
     * @param request - данные участника
     * @return сохранённый участник
     */
    public Person register(PersonRequest request) {
        return _personRepository.save(new Person(request, PersonRole.PARTICIPANT));
    }
}
