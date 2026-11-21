package com.kulikov.conference_app.model;

import java.util.UUID;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Человек, работающий с системой. Конкретная роль определяется подклассом,
 * все они хранятся в одной таблице person, роль лежит в колонке role
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")
public abstract class Person {

    @Id
    private String id = UUID.randomUUID().toString();

    private String fullName;
    private String email;
    private String passwordHash;

    protected Person() {
    }

    protected Person(String fullName, String email, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /**
     * Роль человека в системе
     */
    public abstract PersonRole getRole();

    // ------------- Геттеры ----------------

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
