package com.kulikov.conference_app.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kulikov.conference_app.model.DirectionResponse;
import com.kulikov.conference_app.model.Direction;
import com.kulikov.conference_app.repositories.DirectionRepository;

/**
 * HTTP слой для получения направлений
 */
@RestController
@RequestMapping("/directions")
public class DirectionController {

    private final DirectionRepository _directionRepository;

    public DirectionController(DirectionRepository directionRepository) {
        this._directionRepository = directionRepository;
    }

    /**
     * Возвращает 200 со списком направлений; 401 если запрос не идентифицирован
     */
    @GetMapping
    List<DirectionResponse> getDirections() {
        List<Direction> directions = _directionRepository.findAll();
        List<DirectionResponse> result = new ArrayList<>();

        for (Direction direction : directions) {
            result.add(DirectionResponse.from(direction));
        }

        return result;
    }
}
