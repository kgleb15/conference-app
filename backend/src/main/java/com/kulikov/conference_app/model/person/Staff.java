package com.kulikov.conference_app.model.person;

import com.kulikov.conference_app.dto.request.StaffRequest;
import com.kulikov.conference_app.model.Direction;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 * Сотрудник направления: организатор или член экспертной комиссии
 */
@Entity
public abstract class Staff extends Person {

    @ManyToOne
    private Direction direction;

    protected Staff() {
    }

    protected Staff(StaffRequest r, String passwordHash, Direction direction) {
        super(r.fullName(), r.email(), passwordHash);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
