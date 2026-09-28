package com.kulikov.conference_app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final List<Application> applications = new ArrayList<>();

    /**
     * Возвращает список всех заявок
     */
    @GetMapping
    Iterable<Application> getApplications() {
        return applications;
    }

    /**
     * Возвращает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return найденная заявка или empty, если не найдена
     */
    @GetMapping("/{id}")
    Optional<Application> getApplication(@PathVariable String id) {
        return find(id);
    }

    @PostMapping
    ResponseEntity<Application> postApplication(@RequestBody ApplicationRequest request) {
        Application application = new Application(request);
        applications.add(application);
        return new ResponseEntity<>(application, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    ResponseEntity<Application> putApplication(@PathVariable String id, @RequestBody ApplicationRequest request) {
        Optional<Application> found = find(id);

        if (found.isEmpty()) {
            return postApplication(request); // Создание новой заявки
        }

        Application application = found.get();

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) { // Если отозвана
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        application.update(request);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    /**
     * Отзывает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return 200 с отозванной заявкой если успешно отозвана; 404 если не найдена
     */
    @PatchMapping("/{id}/withdraw")
    ResponseEntity<Application> withdraw(@PathVariable String id) {
        Optional<Application> found = find(id);
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Application application = found.get();
        application.withdraw(); // Отзываем заявку

        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    /**
     * Ищет заявку по id
     * 
     * @param id - идентификатор заявки
     * @return найденная заявка или empty
     */
    private Optional<Application> find(String id) {
        for (Application application : applications) {
            if (application.getId().equals(id)) {
                return Optional.of(application);
            }
        }

        return Optional.empty();
    }
}
