package com.kulikov.conference_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Direction;

@Repository
@Profile("db")
public class DbDirectionRepository implements DirectionRepository {
    private final DirectionJpaRepository _jpa;

    public DbDirectionRepository(DirectionJpaRepository jpa) {
        this._jpa = jpa;
    }

    @Override
    public List<Direction> findAll() {
        return _jpa.findAll(Sort.by("id"));
    }

    @Override
    public Optional<Direction> findById(int id) {
        return _jpa.findById(id);
    }
}
