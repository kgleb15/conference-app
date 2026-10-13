package com.kulikov.conference_app.services;

import java.util.List;

import com.kulikov.conference_app.model.Application;

/**
 * Результат операции сервиса заявок над списком.
 *
 * @param applications - список заявок или null
 * @param status       - исход операции
 */
public record ServiceListResult(List<Application> applications, ServiceStatus status) {
}
