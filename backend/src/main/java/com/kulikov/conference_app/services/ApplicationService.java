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
import com.kulikov.conference_app.model.ApplicationAuthor;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.model.Person;
import com.kulikov.conference_app.model.Direction;
import com.kulikov.conference_app.model.Participant;
import com.kulikov.conference_app.model.PersonRole;
import com.kulikov.conference_app.model.Staff;
import com.kulikov.conference_app.repositories.ApplicationRepository;
import com.kulikov.conference_app.repositories.DirectionRepository;
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
    DirectionRepository _directionRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
            PersonRepository personRepository, DirectionRepository directionRepository,
            CurrentUser currentUser, ConferenceProperties conferenceProperties, Clock clock) {
        this._applicationRepository = applicationRepository;
        this._personRepository = personRepository;
        this._currentUser = currentUser;
        this._conferenceProperties = conferenceProperties;
        this._clock = clock;
        this._directionRepository = directionRepository;
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
            if (isAuthor(application, owner)) {
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
        if (found.isEmpty() || !isAuthor(found.get(), person.get())) {
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
     *         пользователь не участник; INVALID_DIRECTION, если направление не
     *         существует; LIMIT_EXCEEDED, если уже 2 действующие заявки в этом
     *         направлении
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
        Optional<Direction> direction = _directionRepository.findById(request.directionId());
        if (direction.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.INVALID_DIRECTION);
        }
        Participant coAuthor = null;
        if (request.coAuthor() != null) {
            Optional<Person> found = _personRepository.findByEmail(request.coAuthor().email());
            if (found.isEmpty() || found.get().getRole() != PersonRole.PARTICIPANT
                    || found.get().getId().equals(person.get().getId())) {
                return new ServiceResult(null, ServiceStatus.INVALID_COAUTHOR);
            }
            coAuthor = (Participant) found.get();
        }
        if (countActiveApplications(person.get(), direction.get()) >= MAX_APPLICATIONS_PER_PERSON) {
            return new ServiceResult(null, ServiceStatus.LIMIT_EXCEEDED);
        }
        if (coAuthor != null && countActiveApplications(coAuthor, direction.get()) >= MAX_APPLICATIONS_PER_PERSON) {
            return new ServiceResult(null, ServiceStatus.COAUTHOR_LIMIT_EXCEEDED);
        }
        Application saved = _applicationRepository
                .save(new Application((Participant) person.get(), coAuthor, direction.get(), request));
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
     *         пользователь не участник или не владелец заявки; INVALID_STATE если
     *         решение по заявке уже принято
     */
    public ServiceResult edit(String id, ApplicationRequest request) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.PARTICIPANT);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isAuthor(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (!application.isOwner(person.get())) {
            return new ServiceResult(null, ServiceStatus.FORBIDDEN);
        }
        ServiceStatus modifiable = checkModifiable(application);
        if (modifiable != ServiceStatus.OK) {
            return new ServiceResult(null, modifiable);
        }
        if (!application.canBeEdited()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
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
        if (found.isEmpty() || !isAuthor(found.get(), person.get())) {
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
     * Возвращает все заявки для организатора (для его направления)
     *
     * @param status - фильтр по статусу заявки или null, если без фильтра
     * @param edited - true - только изменённые; false - только неизменённые; null -
     *               без фильтра
     * @return OK и список заявок или UNAUTHORIZED если пользователя нет; FORBIDDEN
     *         если пользователь не организатор
     */
    public ServiceListResult findAllForOrganizer(ApplicationStatus status, Boolean edited) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceListResult(null, access);
        }

        List<Application> filtered = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            boolean statusMatches = status == null || application.getStatus() == status;
            boolean editedMatches = edited == null || (application.getEditCount() > 0) == edited;
            if (statusMatches && editedMatches && inSameDirection(application, person.get())) {
                filtered.add(application);
            }
        }

        return new ServiceListResult(filtered, ServiceStatus.OK);
    }

    /**
     * Утверждает заявку (для его направления)
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
        if (found.isEmpty() || !inSameDirection(found.get(), person.get())) {
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
     * Отклоняет заявку (для его направления)
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
        if (found.isEmpty() || !inSameDirection(found.get(), person.get())) {
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
        if (found.isEmpty() || !inSameDirection(found.get(), person.get())) {
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
     * Переводит заявку в другое направление
     *
     * @param id          - идентификатор заявки
     * @param directionId - номер направления, в которое перевод
     * @return OK и заявка; NOT_FOUND если заявки нет или она не из вашего
     *         направления; INVALID_STATE если по заявке уже нельзя принять решение
     *         или направление то же самое; INVALID_DIRECTION если такого
     *         направления нет; LIMIT_EXCEEDED если у автора уже 2 действующие
     *         заявки в новом направлении;
     */
    public ServiceResult transfer(String id, int directionId) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !inSameDirection(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (!application.canBeModerated()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }

        Optional<Direction> target = _directionRepository.findById(directionId);
        if (target.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.INVALID_DIRECTION);
        }
        if (target.get().getId() == application.getDirection().getId()) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }
        for (ApplicationAuthor author : application.getAuthors()) {
            if (countActiveApplications(author.getPerson(), target.get()) >= MAX_APPLICATIONS_PER_PERSON) {
                return new ServiceResult(null, ServiceStatus.LIMIT_EXCEEDED);
            }
        }

        application.requestTransfer(target.get(), Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Заявки, которые другие направления передали в направление текущего организатора
     */
    public ServiceListResult findIncomingTransfers() {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceListResult(null, access);
        }

        List<Application> incoming = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            if (isIncomingFor(application, person.get())) {
                incoming.add(application);
            }
        }
        return new ServiceListResult(incoming, ServiceStatus.OK);
    }

    /**
     * Принимает заявку, переданную из другого направления
     *
     * @return OK; NOT_FOUND если заявка не передана в ваше направление;
     *         LIMIT_EXCEEDED если у автора уже 2 заявки в вашем направлении
     */
    public ServiceResult acceptTransfer(String id) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isIncomingFor(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        Direction myDirection = ((Staff) person.get()).getDirection();
        for (ApplicationAuthor author : application.getAuthors()) {
            if (countActiveApplications(author.getPerson(), myDirection) >= MAX_APPLICATIONS_PER_PERSON) {
                return new ServiceResult(null, ServiceStatus.LIMIT_EXCEEDED);
            }
        }

        application.acceptTransfer(Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Отклоняет передачу заявки
     *
     * @return OK; NOT_FOUND если заявка не передана в ваше направление
     */
    public ServiceResult declineTransfer(String id) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !isIncomingFor(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        application.cancelTransfer(Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Отменяет передачу заявки своего направления, пока её не приняли
     *
     * @return OK; NOT_FOUND если заявки нет в вашем направлении; INVALID_STATE
     *         если заявка не передаётся
     */
    public ServiceResult cancelTransfer(String id) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.ORGANIZER);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || !inSameDirection(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        if (application.getStatus() != ApplicationStatus.TRANSFER_PENDING) {
            return new ServiceResult(null, ServiceStatus.INVALID_STATE);
        }

        application.cancelTransfer(Instant.now(_clock));
        return new ServiceResult(_applicationRepository.save(application), ServiceStatus.OK);
    }

    /**
     * Возвращает утверждённые заявки для члена экспертной комиссии (для его
     * направления)
     *
     * @return OK и список утверждённых заявок; UNAUTHORIZED если запрос не
     *         идентифицирован; FORBIDDEN если пользователь не эксперт
     */
    public ServiceListResult findAllForExpert() {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.EXPERT);
        if (access != ServiceStatus.OK) {
            return new ServiceListResult(null, access);
        }

        List<Application> approved = new ArrayList<>();
        for (Application application : _applicationRepository.findAll()) {
            if (application.getStatus() == ApplicationStatus.APPROVED && inSameDirection(application, person.get())) {
                approved.add(application);
            }
        }
        return new ServiceListResult(approved, ServiceStatus.OK);
    }

    /**
     * Возвращает утверждённую заявку для члена экспертной комиссии
     *
     * @param id - идентификатор заявки
     * @return OK и заявка; NOT_FOUND если заявки нет или она не утверждена;
     *         UNAUTHORIZED если запрос не идентифицирован; FORBIDDEN если
     *         пользователь не эксперт
     */
    public ServiceResult getForExpert(String id) {
        Optional<Person> person = currentPerson();
        ServiceStatus access = checkRole(person, PersonRole.EXPERT);
        if (access != ServiceStatus.OK) {
            return new ServiceResult(null, access);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty() || found.get().getStatus() != ApplicationStatus.APPROVED
                || !inSameDirection(found.get(), person.get())) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }
        return new ServiceResult(found.get(), ServiceStatus.OK);
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
     * Проверяет, является ли человек автором или соавтором заявки
     *
     * @param a      - заявка
     * @param person - предполагаемый автор (соавтор)
     * @return true, если является; false если нет
     */
    private boolean isAuthor(Application a, Person person) {
        return a.isAuthor(person);
    }

    /**
     * Проверяет, что заявка относится к направлению организатора или эксперта
     */
    private boolean inSameDirection(Application a, Person person) {
        return person instanceof Staff staff
                && a.getDirection().getId() == staff.getDirection().getId();
    }

    private boolean isIncomingFor(Application a, Person person) {
        return person instanceof Staff staff
                && a.getStatus() == ApplicationStatus.TRANSFER_PENDING
                && a.getTransferTarget() != null
                && a.getTransferTarget().getId() == staff.getDirection().getId();
    }

    /**
     * Считает действующие заявки участника (без отозванных и отклоненных)
     *
     * @param owner - участник
     * @return количество действующих заявок
     */
    private int countActiveApplications(Person owner, Direction direction) {
        int count = 0;
        for (Application application : _applicationRepository.findAll()) {
            boolean active = application.getStatus() != ApplicationStatus.WITHDRAWN
                    && application.getStatus() != ApplicationStatus.REJECTED;
            boolean sameDirection = application.getDirection().getId() == direction.getId();
            if (active && sameDirection && isAuthor(application, owner)) {
                count++;
            }
        }
        return count;
    }
}