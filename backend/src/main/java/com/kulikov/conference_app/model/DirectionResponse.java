package com.kulikov.conference_app.model;

public record DirectionResponse(int id, String name) {
    public static DirectionResponse from(Direction d) {
        return new DirectionResponse(d.getId(), d.getName());
    }
}
