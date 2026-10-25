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

    private static final int MAX_APPLICATIONS_PER_PERSON = 2;

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
     *         идентифицирован; DEADLINE_PASSED если дедлайн прошёл; FORBIDDEN если
     *         пользователь не участник; LIMIT_EXCEEDED если у участника уже 2
     *         действующие заявки
     */
    public ServiceResult submit(ApplicationRequest request) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.PARTICIPANT);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }
        if (countActiveApplications(person.get()) >= MAX_APPLICATIONS_PER_PERSON) {
            return new ServiceResult(null, ServiceStatus.LIMIT_EXCEEDED);
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
     *         UNAUTHORIZED если запрос не идентифицирован; FORBIDDEN если
     *         пользователь не участник; INVALID_STATE если решение по заявке уже
     *         принято
     */
    public ServiceResult edit(String id, ApplicationRequest request) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.PARTICIPANT);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isOwnedBy(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        ServiceStatus modifiable = checkModifiable(application);
        if (modifiable != ServiceStatus.OK) {
            return new ServiceResult(null, modifiable);
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
     *         идентифицирован; FORBIDDEN если пользователь не участник;
     *         INVALID_STATE если решение по заявке уже принято
     */
    public ServiceResult withdraw(String id) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.PARTICIPANT);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isOwnedBy(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return new ServiceResult(application, ServiceStatus.OK); // уже отозвана
        }
        ServiceStatus modifiable = checkModifiable(application);
        if (modifiable != ServiceStatus.OK) {
            return new ServiceResult(null, modifiable);
        }

        application.withdraw();
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Возвращает все заявки для организатора
     *
     * @param status - фильтр по статусу заявки или null, если без фильтра
     * @param edited - true - только изменённые; false - только неизменённые; null -
     *               без фильтра
     * @return OK и список заявок или UNAUTHORIZED если пользователя нет; FORBIDDEN
     *         если пользователь не организатор
     */
    public ServiceListResult findAllForOrganizer(ApplicationStatus status, Boolean edited) {
        ServiceStatus access = checkRole(currentPerson(), PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceListResult(null, access);
        }

        List<Application> filtered = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            boolean statusMatches = status == null || application.getStatus() == status;
            boolean editedMatches = edited == null || (application.getEditCount() > 0) == edited;
            if (statusMatches && editedMatches) {
                filtered.add(application);
            }
        }

        return new ServiceListResult(filtered, ServiceStatus.OK);
    }

    /**
     * Утверждает заявку
     *
     * @param id      - идентификатор заявки
     * @param comment - комментарий организатора
     * @return OK и утверждённая заявка; NOT_FOUND если такой заявки нет;
     *         INVALID_STATE если по заявке уже нельзя принять решение;
     *         UNAUTHORIZED если запрос не идентифицирован;
     *         FORBIDDEN если пользователь не организатор
     */
    public ServiceResult approve(String id, String comment) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (!application.canBeModerated()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }

        application.approve(person.get(), comment, Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Отклоняет заявку
     *
     * @param id      - идентификатор заявки
     * @param comment - комментарий организатора
     * @return OK и утверждённая заявка; NOT_FOUND если такой заявки нет;
     *         INVALID_STATE если по заявке уже нельзя принять решение;
     *         UNAUTHORIZED если запрос не идентифицирован;
     *         FORBIDDEN если пользователь не организатор
     */
    public ServiceResult reject(String id, String comment) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (!application.canBeModerated()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }

        application.reject(person.get(), comment, Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Отправляет заявку на доработку (участник может править её до T2)
     *
     * @param id      - идентификатор заявки
     * @param comment - комментарий организатора
     * @return OK и утверждённая заявка; NOT_FOUND если такой заявки нет;
     *         INVALID_STATE если по заявке уже нельзя принять решение;
     *         UNAUTHORIZED если запрос не идентифицирован;
     *         FORBIDDEN если пользователь не организатор
     */
    public ServiceResult requestRevision(String id, String comment) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (!application.canBeModerated()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }

        application.requestRevision(person.get(), comment, Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
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
     * Проверяет, прошёл ли дедлайн, действующий для заявки в её текущем статусе
     *
     * @param a - заявка
     * @return true, если срок уже прошёл; false - если нет
     */
    private boolean isDeadlinePassedFor(Application a) {
        Instant deadline = a.getStatus() == ApplicationStatus.REVISION_REQUESTED
                ? _conferenceProperties.getDeadlineT2()
                : _conferenceProperties.getDeadlineT1();
        return Instant.now(_clock).isAfter(deadline);
    }

    /**
     * Проверяет, может ли участник сейчас изменить заявку
     *
     * @param a - заявка
     * @return OK; WITHDRAWN если отозвана; INVALID_STATE если по ней уже принято
     *         решение; DEADLINE_PASSED если срок вышел
     */
    private ServiceStatus checkModifiable(Application a) {
        if (a.getStatus() == ApplicationStatus.WITHDRAWN) {
            return ServiceStatus.WITHDRAWN;
        }
        if (a.getStatus() == ApplicationStatus.APPROVED || a.getStatus() == ApplicationStatus.REJECTED) {
            return ServiceStatus.INVALID_STATE;
        }
        if (isDeadlinePassedFor(a)) {
            return ServiceStatus.DEADLINE_PASSED;
        }
        return ServiceStatus.OK;
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
     * Проверяет, что пользователь имеет нужную роль
     *
     * @param person - текущий пользователь
     * @param role   - требуемая роль
     * @return OK если всё в порядке; UNAUTHORIZED если пользователя нет; FORBIDDEN
     *         если роль не подходит
     */
    private ServiceStatus checkRole(Optional<Person> person, PersonRole role) {
        if (person.isEmpty()) {
            return ServiceStatus.UNAUTHORIZED;
        }
        if (person.get().getRole() != role) {
            return ServiceStatus.FORBIDDEN;
        }
        return ServiceStatus.OK;
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

    /**
     * Считает действующие заявки участника (без отозванных и отклоненных)
     *
     * @param owner - участник
     * @return количество действующих заявок
     */
    private int countActiveApplications(Person owner) {
        int count = 0;
        for (Application application : _applicationRepository.findAll()) {
            boolean active = application.getStatus() != ApplicationStatus.WITHDRAWN
                    && application.getStatus() != ApplicationStatus.REJECTED;
            if (active && isOwnedBy(application, owner)) {
                count++;
            }
        }
        return count;
    }
}