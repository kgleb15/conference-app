package com.kulikov.conference_app.model;

import jakarta.persistence.Embeddable;

/**
 * Научный руководитель
 */
@Embeddable
public class Supervisor {
    private String fullName;
    private String workplace;

    protected Supervisor() {
    }

    public Supervisor(String fullName, String workplace) {
        this.fullName = fullName;
        this.workplace = workplace;
    }

    public String getFullName() {
        return fullName;
    }

    public String getWorkplace() {
        return workplace;
    }
}
