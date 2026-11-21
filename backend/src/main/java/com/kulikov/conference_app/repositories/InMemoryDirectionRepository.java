package com.kulikov.conference_app.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Direction;

/**
 * Хранилище направлений в памяти
 */
@Repository
@Profile("!db")
public class InMemoryDirectionRepository implements DirectionRepository {

    private static final String[] NAMES = {
            "Электронные устройства и системы",
            "Прогрессивные технологии в машиностроении, устройства и средства автоматизации",
            "Конструкционные материалы и технологии",
            "Химия, химические процессы и технологии, проблемы экологии",
            "Динамика и надежность машин, механизмов, конструкций",
            "Дороги и автомобильный транспорт",
            "Программно-информационное обеспечение",
            "Экономика и управление",
            "Социальные процессы и гуманитарные знания",
            "Проблемы пищевой технологии",
            "Архитектура и дизайн",
            "Проблемы развития урбанизированных территорий",
            "Проблемы жилищно-коммунального хозяйства",
            "Технологии и материалы строительной индустрии",
            "Надёжность строительных конструкций и инженерные изыскания",
            "Роботы, мехатроника и робототехнические системы",
            "Цифровые технологии в урбанистике, архитектуре и строительстве"
    };

    private final List<Direction> _storage = new ArrayList<>();

    public InMemoryDirectionRepository() {
        for (int i = 0; i < NAMES.length; i++) {
            _storage.add(new Direction(i + 1, NAMES[i]));
        }
    }

    @Override
    public List<Direction> findAll() {
        return new ArrayList<>(_storage);
    }

    @Override
    public Optional<Direction> findById(int id) {
        return _storage.stream().filter(d -> d.getId() == id).findFirst();
    }
}
