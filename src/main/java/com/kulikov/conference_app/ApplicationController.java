package com.kulikov.conference_app;

import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationRepository _applicationRepository;
    private final ConferenceProperties _conferenceProperties;

    public ApplicationController(ApplicationRepository applicationRepository,
            ConferenceProperties conferenceProperties) {
        this._applicationRepository = applicationRepository;
        this._conferenceProperties = conferenceProperties;
    }

    /**
     * Возвращает список всех заявок
     */
    @GetMapping
    Iterable<Application> getApplications() {
        return _applicationRepository.findAll();
    }

    /**
     * Возвращает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return найденная заявка или empty, если не найдена
     */
    @GetMapping("/{id}")
    Optional<Application> getApplication(@PathVariable String id) {
        return _applicationRepository.findById(id);
    }

    /**
     * Создает новую заявку
     * 
     * @param request - данные новой заявки
     * @return 201 с созданной заявкой; 403 если дата некорректна; 400 если некорректны поля запроса
     */
    @PostMapping
    ResponseEntity<Application> submit(@Valid @RequestBody ApplicationRequest request) {
        if (Instant.now().isAfter(_conferenceProperties.getDeadlineT1())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Application application = _applicationRepository.save(new Application(request));
        return new ResponseEntity<>(application, HttpStatus.CREATED);
    }

    /**
     * Обновляет заявку или создает новую
     * 
     * @param id      - идентификатор заявки
     * @param request - новые данные заявки
     * @return 201 если создана новая; 409 если отозвана; 200 если успешно обновлена; 403 если дата некорректна; 400 если некорректны поля запроса
     */
    @PutMapping("/{id}")
    ResponseEntity<Application> edit(@PathVariable String id, @Valid @RequestBody ApplicationRequest request) {
        if (Instant.now().isAfter(_conferenceProperties.getDeadlineT1())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Application> found = _applicationRepository.findById(id);

        if (found.isEmpty()) {
            return submit(request); // Создание новой заявки
        }

        Application application = found.get();

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) { // Если отозвана
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        application.update(request);
        _applicationRepository.save(application);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    /**
     * Отзывает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return 200 с отозванной заявкой если успешно отозвана; 404 если не найдена;  403 если дата некорректна
     */
    @PatchMapping("/{id}/withdraw")
    ResponseEntity<Application> withdraw(@PathVariable String id) {
        if (Instant.now().isAfter(_conferenceProperties.getDeadlineT1())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Application> found = _applicationRepository.findById(id);
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Application application = found.get();
        application.withdraw(); // Отзываем заявку
        _applicationRepository.save(application);

        return new ResponseEntity<>(application, HttpStatus.OK);
    }
}
