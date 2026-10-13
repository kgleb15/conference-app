package com.kulikov.conference_app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kulikov.conference_app.model.Application;

public interface ApplicationJpaRepository extends JpaRepository<Application, String> {

}
