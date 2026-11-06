package com.kulikov.conference_app.model;

public record PersonResponse(String id, String fullName,
        String email, String phone, PersonRole role) {

    /**
     * Собирает ответ из сущности Person
     */
    public static PersonResponse from(Person p) {
        return new PersonResponse(p.getId(), p.getFullName(),
                p.getEmail(), p.getPhone(), p.getRole());
    }
}
