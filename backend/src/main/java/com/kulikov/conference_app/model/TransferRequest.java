package com.kulikov.conference_app.model;

import jakarta.validation.constraints.NotNull;

/**
 * Запрос на перевод заявки в другое направление
 */
public record TransferRequest(@NotNull Integer directionId) {
}
