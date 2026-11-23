package com.kulikov.conference_app.repositories.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kulikov.conference_app.repositories.ApplicationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.kulikov.conference_app.model.application.Application;

/**
 * Реализация ApplicationRepository через хранение в оперативной памяти
 */
@Repository
@Profile("!db")
public class InMemoryApplicationRepository implements ApplicationRepository {

    private final Map<String, Application> _storage = new LinkedHashMap<>();

    @Override
    public List<Application> findAll() {
        return new ArrayList<>(_storage.values());
    }

    @Override
    public Optional<Application> findById(String id) {
        return Optional.ofNullable(_storage.get(id));
    }

    @Override
    public Application save(Application application) {
        _storage.put(application.getId(), application);
        return application;
    }
}