package com.kulikov.conference_app.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

/**
 * Человек, работающий с системой
 */
@Entity
public class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    private String fullName;
    private String groupName;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private PersonRole role;

    protected Person() { }

    public Person(PersonRequest r, PersonRole role) {
        this.fullName = r.fullName();
        this.groupName = r.groupName();
        this.email = r.email();
        this.phone = r.phone();
        this.role = role;
    }

    // ------------- Геттеры ----------------

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public PersonRole getRole() {
        return role;
    }
}
