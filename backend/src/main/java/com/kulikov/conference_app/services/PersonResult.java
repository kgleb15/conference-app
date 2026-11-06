package com.kulikov.conference_app.services;

import com.kulikov.conference_app.model.Person;

/**
 * Результат операции сервиса людей.
 *
 * @param person - созданный человек или null, если создать не удалось
 * @param status - статус операции
 */
public record PersonResult(Person person, ServiceStatus status) {
}
