package com.kulikov.conference_app.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kulikov.conference_app.model.person.Person;
import com.kulikov.conference_app.model.person.Expert;
import com.kulikov.conference_app.model.person.Organizer;
import com.kulikov.conference_app.model.person.Participant;
import com.kulikov.conference_app.dto.request.ParticipantRequest;
import com.kulikov.conference_app.model.person.PersonRole;
import com.kulikov.conference_app.model.person.Staff;
import com.kulikov.conference_app.model.Direction;
import com.kulikov.conference_app.dto.request.StaffRequest;
import com.kulikov.conference_app.repositories.DirectionRepository;
import com.kulikov.conference_app.repositories.PersonRepository;
import java.util.Optional;

/**
 * Сервис работы с людьми
 */
@Service
public class PersonService {

    private final PersonRepository _personRepository;
    private final PasswordEncoder _passwordEncoder;
    private final DirectionRepository _directionRepository;

    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder,
            DirectionRepository directionRepository) {
        this._personRepository = personRepository;
        this._passwordEncoder = passwordEncoder;
        this._directionRepository = directionRepository;
    }

    /**
     * Регистрирует нового участника конференции
     *
     * @return CREATED и участник; CONFLICT если email уже занят
     */
    public PersonResult register(ParticipantRequest request) {
        if (_personRepository.findByEmail(request.email()).isPresent()) {
            return new PersonResult(null, ServiceStatus.CONFLICT);
        }
        String hash = _passwordEncoder.encode(request.password());
        Person saved = _personRepository.save(new Participant(request, hash));
        return new PersonResult(saved, ServiceStatus.CREATED);
    }

    /**
     * Регистрирует нового организатора направления
     *
     * @return CREATED и организатор; CONFLICT если email уже занят;
     *         INVALID_DIRECTION если направления нет
     */
    public PersonResult registerOrganizer(StaffRequest request) {
        return createStaff(request, PersonRole.ORGANIZER);
    }

    /**
     * Регистрирует нового члена экспертной комиссии направления
     *
     * @return CREATED и эксперт; CONFLICT если email уже занят;
     *         INVALID_DIRECTION если направления нет
     */
    public PersonResult registerExpert(StaffRequest request) {
        return createStaff(request, PersonRole.EXPERT);
    }

    private PersonResult createStaff(StaffRequest request, PersonRole role) {
        Optional<Direction> direction = _directionRepository.findById(request.directionId());
        if (direction.isEmpty()) {
            return new PersonResult(null, ServiceStatus.INVALID_DIRECTION);
        }
        if (_personRepository.findByEmail(request.email()).isPresent()) {
            return new PersonResult(null, ServiceStatus.CONFLICT);
        }
        String hash = _passwordEncoder.encode(request.password());
        Staff staff = role == PersonRole.ORGANIZER
                ? new Organizer(request, hash, direction.get())
                : new Expert(request, hash, direction.get());
        return new PersonResult(_personRepository.save(staff), ServiceStatus.CREATED);
    }

    /**
     * Находит человека по id
     *
     * @param id - идентификатор человека
     * @return человек или empty, если не найден
     */
    public Optional<Person> findById(String id) {
        return _personRepository.findById(id);
    }
}
