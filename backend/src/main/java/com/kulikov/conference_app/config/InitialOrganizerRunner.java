package com.kulikov.conference_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.kulikov.conference_app.dto.request.StaffRequest;
import com.kulikov.conference_app.services.PersonService;

/**
 * При старте создаёт первого организатора из настроек
 */
@Component
public class InitialOrganizerRunner implements ApplicationRunner {

    private final PersonService _personService;
    private final String _email;
    private final String _password;
    private final int _directionId;

    public InitialOrganizerRunner(PersonService personService,
            @Value("${conference.organizer.email:}") String email,
            @Value("${conference.organizer.password:}") String password,
            @Value("${conference.organizer.direction-id:7}") int directionId) {
        this._personService = personService;
        this._email = email;
        this._password = password;
        this._directionId = directionId;
    }

    /**
     * метод вызывается один раз после запуска приложения
     */
    @Override
    public void run(ApplicationArguments args) {
        if (_email.isBlank() || _password.isBlank()) {
            return;
        }
        _personService.registerOrganizer(new StaffRequest("Организатор", _email, _password, _directionId));
    }
}
