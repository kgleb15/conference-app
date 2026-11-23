package com.kulikov.conference_app.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Запрос на перевод заявки в другое направление
 */
public record TransferRequest(@NotNull Integer directionId) {
}
