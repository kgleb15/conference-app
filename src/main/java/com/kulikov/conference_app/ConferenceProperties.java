package com.kulikov.conference_app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Instant;

@ConfigurationProperties(prefix = "conference")
public class ConferenceProperties {

    private Instant deadlineT1;

    /**
     * Возвращает крайний срок подачи/редактирования/отзыва заявки
     */
    public Instant getDeadlineT1() {
        return deadlineT1;
    }

    public void setDeadlineT1(Instant deadlineT1) {
        this.deadlineT1 = deadlineT1;
    }
}
