package com.kulikov.conference_app.model.person;

import com.kulikov.conference_app.dto.request.StaffRequest;
import com.kulikov.conference_app.model.Direction;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Организатор направления
 */
@Entity
@DiscriminatorValue("ORGANIZER")
public class Organizer extends Staff {

    protected Organizer() {
    }

    public Organizer(StaffRequest r, String passwordHash, Direction direction) {
        super(r, passwordHash, direction);
    }

    @Override
    public PersonRole getRole() {
        return PersonRole.ORGANIZER;
    }
}
