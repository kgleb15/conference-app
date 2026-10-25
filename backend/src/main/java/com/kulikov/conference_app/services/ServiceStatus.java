package com.kulikov.conference_app.services;

/**
 * Результат операции сервиса заявок.
 */
public enum ServiceStatus {
    OK,
    CREATED,
    NOT_FOUND,
    DEADLINE_PASSED,
    WITHDRAWN,
    UNAUTHORIZED,
    FORBIDDEN, // недостаточно прав
    INVALID_STATE // действие недопустимо в текущем статусе
}
