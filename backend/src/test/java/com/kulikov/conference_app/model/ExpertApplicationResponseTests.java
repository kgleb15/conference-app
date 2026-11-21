package com.kulikov.conference_app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class ExpertApplicationResponseTests {

    @Test
    void test_from_doesNotContainPersonalData() {
        Participant owner = new Participant(new ParticipantRequest("Иванов Иван Иванович",
                "secret@yandex.ru", "89990001122", "password123"), "hash");
        Direction direction = new Direction(7, "Программно-информационное обеспечение");
        Application app = new Application(owner, direction,
                new ApplicationRequest("Название", "Аннотация", "Текст", "ПрИн-467",
                        ApplicantCategory.STUDENT, FundingType.CONTRACT, 7));

        ExpertApplicationResponse response = ExpertApplicationResponse.from(app);

        assertEquals("Название", response.title());
        assertEquals("Аннотация", response.annotation());
        assertEquals("Текст", response.thesisText());

        String text = response.toString();
        assertFalse(text.contains("Иванов"));
        assertFalse(text.contains("ПрИн-467"));
        assertFalse(text.contains("secret@yandex.ru"));
        assertFalse(text.contains("89990001122"));
        assertEquals(ApplicantCategory.STUDENT, response.applicantCategory());
        assertFalse(text.contains("CONTRACT"));
        assertFalse(text.contains(owner.getId()));
    }
}
