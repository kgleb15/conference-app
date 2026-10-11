package com.kulikov.conference_app.services;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kulikov.conference_app.config.ConferenceProperties;
import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.repositories.ApplicationRepository;

/**
 * Сервис бизнес-логики работы с заявками
 */
@Service
public class ApplicationService {

    private final ApplicationRepository _applicationRepository;
    private final ConferenceProperties _conferenceProperties;
    private final Clock _clock;

    public ApplicationService(ApplicationRepository applicationRepository,
            ConferenceProperties conferenceProperties, Clock clock) {
        this._applicationRepository = applicationRepository;
        this._conferenceProperties = conferenceProperties;
        this._clock = clock;
    }

    /**
     * Возвращает список всех заявок.
     * 
     * @return список заявок (пустой, если заявок нет)
     */
    public List<Application> findAll() {
        return _applicationRepository.findAll();
    }

    /**
     * Возвращает заявку по id.
     *
     * @param id - идентификатор заявки
     * @return результат со статусом OK и найденной заявкой или NOT_FOUND с null
     */
    public ServiceResult get(String id) {
        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }
        return new ServiceResult(found.get(), ServiceStatus.OK);
    }

    /**
     * Создаёт новую заявку.
     *
     * @param request - данные новой заявки
     * @return CREATED и сохранённая заявка или DEADLINE_PASSED и null
     */
    public ServiceResult submit(ApplicationRequest request) {
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }
        Application saved = _applicationRepository.save(new Application(request));
        return new ServiceResult(saved, ServiceStatus.CREATED);
    }

    // TODO: убрать создание новой
    /**
     * Обновляет заявку или создаёт новую, если заявки с таким id нет
     *
     * @param id - идентификатор заявки
     * @param request - новые данные заявки
     * @return CREATED при создании новой, OK при успешном обновлении, 
     *         WITHDRAWN если отозвана, DEADLINE_PASSED если дедлайн прошел 
     */
    public ServiceResult edit(String id, ApplicationRequest request) {
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            Application saved = _applicationRepository.save(new Application(request));
            return new ServiceResult(saved, ServiceStatus.CREATED);
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
     * @return OK если отозвана, NOT_FOUND если заявки нет, DEADLINE_PASSED если дедлайн прошел 
     */
    public ServiceResult withdraw(String id) {
        if (isDeadlinePassed()) {
            return new ServiceResult(null, ServiceStatus.DEADLINE_PASSED);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ServiceResult(null, ServiceStatus.NOT_FOUND);
        }

        Application application = found.get();
        application.withdraw();
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
}