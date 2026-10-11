package com.kulikov.conference_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.Application;

/**
 * Реализация ApplicationRepository через Spring Data JPA
 */
@Repository
@Primary
public class DbApplicationRepository implements ApplicationRepository {
    private final ApplicationJpaRepository _jpa;

    public DbApplicationRepository(ApplicationJpaRepository jpa) {
        this._jpa = jpa;
    }

    @Override
    public List<Application> findAll() {
        return _jpa.findAll();
    }

    @Override
    public Optional<Application> findById(String id) {
        return _jpa.findById(id);
    }

    @Override
    public Application save(Application application) {
        return _jpa.save(application);
    }
}
