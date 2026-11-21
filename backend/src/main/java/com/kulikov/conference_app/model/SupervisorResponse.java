package com.kulikov.conference_app.model;

public record SupervisorResponse(String fullName, String workplace) {
    public static SupervisorResponse from(Supervisor s) {
        return new SupervisorResponse(s.getFullName(), s.getWorkplace());
    }
}
