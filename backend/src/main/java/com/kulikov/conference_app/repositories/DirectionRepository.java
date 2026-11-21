package com.kulikov.conference_app.repositories;

import java.util.List;
import java.util.Optional;

import com.kulikov.conference_app.model.Direction;

/**
 * Интерфейс хранилища направлений
 */
public interface DirectionRepository {
    List<Direction> findAll();

    Optional<Direction> findById(int id);
}
