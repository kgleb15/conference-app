package com.kulikov.conference_app.model.person;

import com.kulikov.conference_app.dto.request.StaffRequest;
import com.kulikov.conference_app.model.Direction;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Эксперт направления
 */
@Entity
@DiscriminatorValue("EXPERT")
public class Expert extends Staff {

    protected Expert() {
    }

    public Expert(StaffRequest r, String passwordHash, Direction direction) {
        super(r, passwordHash, direction);
    }

    @Override
    public PersonRole getRole() {
        return PersonRole.EXPERT;
    }
}
