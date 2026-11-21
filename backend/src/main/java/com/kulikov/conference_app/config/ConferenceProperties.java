package com.kulikov.conference_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "conference")
public class ConferenceProperties {

    @NotNull
    private Instant deadlineT1;

    @NotNull
    private Instant deadlineT2;

    /**
     * Возвращает крайний срок подачи/редактирования/отзыва заявки участником
     */
    public Instant getDeadlineT1() {
        return deadlineT1;
    }

    public void setDeadlineT1(Instant deadlineT1) {
        this.deadlineT1 = deadlineT1;
    }

    /**
     * Возвращает крайний срок доработки заявки
     */
    public Instant getDeadlineT2() {
        return deadlineT2;
    }

    public void setDeadlineT2(Instant deadlineT2) {
        this.deadlineT2 = deadlineT2;
    }

    /**
     * Проверяет, что срок T2 не раньше срока T1
     */
    @AssertTrue(message = "deadlineT2 должен быть не раньше deadlineT1")
    public boolean isDeadlinesOrdered() {
        return !deadlineT2.isBefore(deadlineT1);
    }
}
