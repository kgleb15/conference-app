package com.kulikov.conference_app.dto.response;

import com.kulikov.conference_app.model.Direction;

public record DirectionResponse(int id, String name) {
    public static DirectionResponse from(Direction d) {
        return new DirectionResponse(d.getId(), d.getName());
    }
}
