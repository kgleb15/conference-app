package com.kulikov.conference_app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import com.kulikov.conference_app.auth.CurrentUser;
import com.kulikov.conference_app.model.Person;

import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.model.PersonResponse;
import com.kulikov.conference_app.services.PersonService;
import com.kulikov.conference_app.services.PersonResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonService _personService;
    private final CurrentUser _currentUser;

    public PersonController(PersonService personService, CurrentUser currentUser) {
        this._personService = personService;
        this._currentUser = currentUser;
    }

    /**
     * Возвращает данные человека, который вошёл
     *
     * @return 200 с данными; 401 если запрос не идентифицирован
     */
    @GetMapping("/me")
    ResponseEntity<PersonResponse> me() {
        Optional<Person> person = _currentUser.personId().flatMap(_personService::findById);
        if (person.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(PersonResponse.from(person.get()), HttpStatus.OK);
    }

    /**
     * Регистрирует участника
     *
     * @param request - данные участника
     * @return 201 с созданным участником; 400 если поля некорректны; 409 если email
     *         уже занят
     */
    @PostMapping
    ResponseEntity<PersonResponse> register(@Valid @RequestBody PersonRequest request) {
        return toResponse(_personService.register(request));
    }

    /**
     * Регистрирует организатора
     *
     * @param request - данные организатора
     * @return 201 с созданным организатором; 400 если поля некорректны; 409 если
     *         email уже занят
     */
    @PostMapping("/organizer")
    ResponseEntity<PersonResponse> registerOrganizer(@Valid @RequestBody PersonRequest request) {
        return toResponse(_personService.registerOrganizer(request));
    }

    /**
     * Регистрирует члена экспертной комиссии
     *
     * @param request - данные эксперта
     * @return 201 с созданным экспертом; 400 если поля некорректны; 409 если email
     *         уже занят
     */
    @PostMapping("/expert")
    ResponseEntity<PersonResponse> registerExpert(@Valid @RequestBody PersonRequest request) {
        return toResponse(_personService.registerExpert(request));
    }

    private ResponseEntity<PersonResponse> toResponse(PersonResult result) {
        switch (result.status()) {
            case CREATED:
                return new ResponseEntity<>(PersonResponse.from(result.person()), HttpStatus.CREATED);
            case CONFLICT:
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            default:
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}