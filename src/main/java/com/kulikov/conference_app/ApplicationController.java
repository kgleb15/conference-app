package com.kulikov.conference_app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        for (Application application : applications) {
            if (application.getId().equals(id)) {
                return Optional.of(application);
            }
        }

        return Optional.empty();
    }
}
