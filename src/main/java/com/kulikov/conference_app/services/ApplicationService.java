package com.kulikov.conference_app.services;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kulikov.conference_app.auth.CurrentUser;
import com.kulikov.conference_app.config.ConferenceProperties;
import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.repositories.ApplicationRepository;
import com.kulikov.conference_app.repositories.PersonRepository;

/**
 * Сервис бизнес-логики работы с заявками
 */
@Service
public class ApplicationService {

    private final ApplicationRepository _applicationRepository;
    private final PersonRepository _personRepository;
    private final CurrentUser _currentUser;
    private final ConferenceProperties _conferenceProperties;
    private final Clock _clock;

    public ApplicationService(ApplicationRepository applicationRepository,
            PersonRepository personRepository, CurrentUser currentUser,
            ConferenceProperties conferenceProperties, Clock clock) {
        this._applicationRepository = applicationRepository;
        this._personRepository = personRepository;
        this._currentUser = currentUser;
        this._conferenceProperties = conferenceProperties;
        this._clock = clock;
    }

    /**
     * Возвращает список заявок текущего участника.
     *
     * @return OK и список своих заявок или UNAUTHORIZED и null
     */
    public ServiceListResult findAll() {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceListResult(null, ServiceStatus.UNAUTHORIZED);
        }

        Person owner = person.get();
        List<Application> ownedApplications = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            if (isOwnedBy(application, owner)) {
                ownedApplications.add(application);
            }
        }
        return new ServiceListResult(ownedApplications, ServiceStatus.OK);
    }

    /**
     * Возвращает заявку по id
     *
     * @param id - идентификатор заявки
     * @return OK и найденная заявка; NOT_FOUND с null если не найдена или чужая;
     *         UNAUTHORIZED если запрос не идентифицирован
     */
    public ServiceResult get(String id) {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.UNAUTHORIZED);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isOwnedBy(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }
        return new ServiceResult(found.get(), ServiceStatus.OK);
    }

    /**
     * Создаёт новую заявку
     *
     * @param request - данные новой заявки
     * @return CREATED и сохранённая заявка; UNAUTHORIZED если запрос не
     *         идентифицирован;
     *         DEADLINE_PASSED если дедлайн прошёл
     */
    public ServiceResult submit(ApplicationRequest request) {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.UNAUTHORIZED);
        }
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }
        Application saved = _applicationRepository.save(new Application(person.get(), request));
        return new ServiceResult(saved, ServiceStatus.CREATED);
    }

    /**
     * Обновляет заявку
     *
     * @param id      - идентификатор заявки
     * @param request - новые данные заявки
     * @return OK если обновлена; NOT_FOUND если не найдена или чужая;
     *         WITHDRAWN если отозвана; DEADLINE_PASSED если дедлайн прошёл;
     *         UNAUTHORIZED если запрос не идентифицирован
     */
    public ServiceResult edit(String id, ApplicationRequest request) {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.UNAUTHORIZED);
        }
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isOwnedBy(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return new ServiceResult(null, ServiceStatus.WITHDRAWN);
        }

        application.update(request);
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Отзывает заявку по id.
     *
     * @param id - идентификатор заявки
     * @return OK если отозвана; NOT_FOUND если не найдена или чужая;
     *         DEADLINE_PASSED если дедлайн прошёл; UNAUTHORIZED если запрос не
     *         идентифицирован
     */
    public ServiceResult withdraw(String id) {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.UNAUTHORIZED);
        }
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isOwnedBy(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        application.withdraw();
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Возвращает все заявки для организатора
     *
     * @param status - фильтр по статусу заявки или null, если без фильтра
     * @return OK и список заявок; UNAUTHORIZED если запрос не идентифицирован;
     *         FORBIDDEN если запрос делает не организатор
     */
    public ServiceListResult findAllForOrganizer(ApplicationStatus status) {
        Optional<Person> person = currentPerson();
        if (person.isEmpty()) {
            return new ServiceListResult(null, ServiceStatus.UNAUTHORIZED);
        }
        if (person.get().getRole() != PersonRole.ORGANIZER) {
            return new ServiceListResult(null, ServiceStatus.FORBIDDEN);
        }

        List<Application> filtered = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            if (status == null || application.getStatus() == status) {
                filtered.add(application);
            }
        }

        return new ServiceListResult(filtered, ServiceStatus.OK);
    }

    /**
     * Проверяет, что текущее время не позже дедлайна T1.
     *
     * @return true, если дедлайн уже прошёл; false - если нет.
     */
    private boolean isDeadlinePassed() {
        return Instant.now(_clock).isAfter(_conferenceProperties.getDeadlineT1());
    }

    /**
     * Находит участника, от имени которого пришёл запрос
     *
     * @return участник или empty, если id неизвестен
     */
    private Optional<Person> currentPerson() {
        Optional<String> personId = _currentUser.personId();
        if (personId.isEmpty()) {
            return Optional.empty();
        }
        return _personRepository.findById(personId.get());
    }

    /**
     * Проверяет, принадлежит ли заявка указанному человеку
     *
     * @param a      - заявка
     * @param person - предполагаемый владелец
     * @return true, если owner совпадает по id; false если нет
     */
    private boolean isOwnedBy(Application a, Person person) {
        return a.getOwner() != null && a.getOwner().getId().equals(person.getId());
    }
}