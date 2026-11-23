package com.kulikov.conference_app.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kulikov.conference_app.model.Direction;

public interface DirectionJpaRepository extends JpaRepository<Direction, Integer> {
}
