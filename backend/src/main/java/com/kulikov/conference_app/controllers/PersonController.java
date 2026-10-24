package com.kulikov.conference_app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonResponse;
import com.kulikov.conference_app.services.PersonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonService _personService;

    public PersonController(PersonService personService) {
        this._personService = personService;
    }

    /**
     * Регистрирует участника
     *
     * @param request - данные участника
     * @return 201 с созданным участником; 400 если поля некорректны
     */
    @PostMapping
    ResponseEntity<PersonResponse> register(@Valid @RequestBody PersonRequest request) {
        return new ResponseEntity<>(PersonResponse.from(_personService.register(request)), HttpStatus.CREATED);
    }

    /**
     * Регистрирует организатора
     * TODO: временно без проверки доступа
     *
     * @param request - данные организатора
     * @return 201 с созданным организатором; 400 если поля некорректны
     */
    @PostMapping("/organizer")
    ResponseEntity<PersonResponse> registerOrganizer(@Valid @RequestBody PersonRequest request) {
        return new ResponseEntity<>(PersonResponse.from(_personService.registerOrganizer(request)), HttpStatus.CREATED);
    }
}