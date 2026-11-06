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
     * @return сохранённый участник
     */
    public Person register(PersonRequest request) {
        return create(request, PersonRole.PARTICIPANT);
    }

    /**
     * Регистрирует нового организатора конференции
     * TODO: временно регистрация открыта для всех
     *
     * @param request - данные организатора
     * @return сохранённый организатор
     */
    public Person registerOrganizer(PersonRequest request) {
        return create(request, PersonRole.ORGANIZER);
    }

    /**
     * Регистрирует нового члена экспертной комиссии
     *
     * @param request - данные эксперта
     * @return сохранённый эксперт
     */
    public Person registerExpert(PersonRequest request) {
        return create(request, PersonRole.EXPERT);
    }

    /**
     * Создаёт человека с заданной ролью
     */
    private Person create(PersonRequest request, PersonRole role) {
        String hash = _passwordEncoder.encode(request.password());
        return _personRepository.save(new Person(request, hash, role));
    }
}
