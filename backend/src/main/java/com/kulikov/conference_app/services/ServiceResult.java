package com.kulikov.conference_app.services;

import com.kulikov.conference_app.model.Application;

/**
 * Результат операции сервиса заявок
 * 
 * @param application - заявка или null, если возвращать нечего
 * @param status      - исход операции
 */
public record ServiceResult(Application application, ServiceStatus status) {

}
