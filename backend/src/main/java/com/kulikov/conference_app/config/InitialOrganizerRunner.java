package com.kulikov.conference_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.kulikov.conference_app.model.PersonRequest;
import com.kulikov.conference_app.services.PersonService;

/**
 * При старте создаёт первого организатора из настроек
 */
@Component
public class InitialOrganizerRunner implements ApplicationRunner {

    private final PersonService _personService;
    private final String _email;
    private final String _password;

    public InitialOrganizerRunner(PersonService personService,
            @Value("${conference.organizer.email:}") String email,
            @Value("${conference.organizer.password:}") String password) {
        this._personService = personService;
        this._email = email;
        this._password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (_email.isBlank() || _password.isBlank()) {
            return;
        }
        _personService.registerOrganizer(new PersonRequest("Организатор", _email, "не указан", _password));
    }
}
