package com.kulikov.conference_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Direction {
    @Id
    private int id; // Номер направления
    private String name;

    protected Direction() {
    }

    public Direction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
