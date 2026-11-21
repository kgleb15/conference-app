package com.kulikov.conference_app.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Участник конференции
 */
@Entity
@DiscriminatorValue("PARTICIPANT")
public class Participant extends Person {

    private String phone;

    protected Participant() {
    }

    public Participant(ParticipantRequest r, String passwordHash) {
        super(r.fullName(), r.email(), passwordHash);
        this.phone = r.phone();
    }

    @Override
    public PersonRole getRole() {
        return PersonRole.PARTICIPANT;
    }

    public String getPhone() {
        return phone;
    }
}
