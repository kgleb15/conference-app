package com.kulikov.conference_app.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Данные соавтора при подаче заявки. (ФИО, телефон и email берутся из его
 * профиля)
 */
public record CoAuthorRequest(@NotBlank @Email String email, @NotBlank String groupName,
        @NotNull FundingType fundingType) {
}
