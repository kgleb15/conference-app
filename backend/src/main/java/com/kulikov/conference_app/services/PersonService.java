package com.kulikov.conference_app.services;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder _passwordEncoder;

    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this._personRepository = personRepository;
        this._passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового участника конференции
     *
     * @param request - данные участника
     * @return CREATED и участник; CONFLICT если email уже занят
     */
    public PersonResult register(PersonRequest request) {
        return create(request, PersonRole.PARTICIPANT);
    }

    /**
     * Регистрирует нового организатора конференции
     *
     * @param request - данные организатора
     * @return CREATED и организатор; CONFLICT если email уже занят
     */
    public PersonResult registerOrganizer(PersonRequest request) {
        return create(request, PersonRole.ORGANIZER);
    }

    /**
     * Регистрирует нового члена экспертной комиссии
     *
     * @param request - данные эксперта
     * @return CREATED и эксперт; CONFLICT если email уже занят
     */
    public PersonResult registerExpert(PersonRequest request) {
        return create(request, PersonRole.EXPERT);
    }

    /**
     * Создаёт человека с заданной ролью
     */
    private PersonResult create(PersonRequest request, PersonRole role) {
        if (_personRepository.findByEmail(request.email()).isPresent()) {
            return new PersonResult(null, ServiceStatus.CONFLICT);
        }
        String hash = _passwordEncoder.encode(request.password());
        Person saved = _personRepository.save(new Person(request, hash, role));
        return new PersonResult(saved, ServiceStatus.CREATED);
    }
}
