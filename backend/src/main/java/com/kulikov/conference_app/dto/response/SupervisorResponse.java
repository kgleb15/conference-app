package com.kulikov.conference_app.dto.response;

import com.kulikov.conference_app.model.application.Supervisor;

public record SupervisorResponse(String fullName, String workplace) {
    public static SupervisorResponse from(Supervisor s) {
        return new SupervisorResponse(s.getFullName(), s.getWorkplace());
    }
}
