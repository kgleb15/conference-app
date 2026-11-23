package com.kulikov.conference_app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.kulikov.conference_app.dto.request.ApplicationRequest;
import com.kulikov.conference_app.dto.request.CoAuthorRequest;
import com.kulikov.conference_app.dto.request.ParticipantRequest;
import com.kulikov.conference_app.dto.request.SupervisorRequest;
import com.kulikov.conference_app.dto.response.ApplicationResponse;
import com.kulikov.conference_app.dto.response.ExpertApplicationResponse;
import com.kulikov.conference_app.model.application.ApplicantCategory;
import com.kulikov.conference_app.model.application.Application;
import com.kulikov.conference_app.model.application.ApplicationStatus;
import com.kulikov.conference_app.model.application.FundingType;
import com.kulikov.conference_app.model.person.Participant;
import org.junit.jupiter.api.Test;

public class ApplicationAuthorsTests {

    private static final Direction DIRECTION = new Direction(7, "Программно-информационное обеспечение");

    private static final SupervisorRequest FIRST = new SupervisorRequest("Дроботов Алексей Владимирович",
            "ВолгГТУ, каф. АПП");
    private static final SupervisorRequest SECOND = new SupervisorRequest("Петрова Анна Сергеевна", "ВолгГУ");

    private Participant participant(String fullName, String email, String phone) {
        return new Participant(new ParticipantRequest(fullName, email, phone, "password123"), "hash");
    }

    private ApplicationRequest request(String title, List<SupervisorRequest> supervisors, CoAuthorRequest coAuthor) {
        return new ApplicationRequest(title, "Аннотация", "Текст тезисов", "ПрИн-467",
                ApplicantCategory.STUDENT, FundingType.BUDGET, 7, supervisors, coAuthor);
    }

    private CoAuthorRequest coAuthor(String email) {
        return new CoAuthorRequest(email, "ПрИн-468", FundingType.CONTRACT);
    }

    @Test
    void test_supervisors() {
        Participant owner = participant("Иванов Иван", "ivan@yandex.ru", "88000000000");
        Application a = new Application(owner, DIRECTION, request("Тема", List.of(FIRST), null));

        assertEquals(1, a.getSupervisors().size());
        assertEquals("Дроботов Алексей Владимирович", a.getSupervisors().get(0).getFullName());
        assertEquals("ВолгГТУ, каф. АПП", a.getSupervisors().get(0).getWorkplace());

        a.update(request("Тема", List.of(FIRST, SECOND), null));
        assertEquals(2, a.getSupervisors().size());
        assertEquals("Дроботов Алексей Владимирович", a.getSupervisors().get(0).getFullName());
        assertEquals("Петрова Анна Сергеевна", a.getSupervisors().get(1).getFullName());
        assertEquals("ВолгГУ", a.getSupervisors().get(1).getWorkplace());

        a.update(request("Тема", List.of(SECOND), null));
        assertEquals(1, a.getSupervisors().size());
        assertEquals("Петрова Анна Сергеевна", a.getSupervisors().get(0).getFullName());
    }

    @Test
    void test_authors_withCoAuthor() {
        Participant owner = participant("Иванов Иван", "ivan@yandex.ru", "88000000000");
        Participant petr = participant("Петров Пётр", "petr@yandex.ru", "89990000000");
        Participant stranger = participant("Чужой Человек", "other@yandex.ru", "87770000000");

        Application a = new Application(owner, petr, DIRECTION,
                request("Тема", List.of(FIRST), coAuthor("petr@yandex.ru")));

        assertEquals(2, a.getAuthors().size());
        assertEquals(0, a.getAuthors().get(0).getAuthorIndex());
        assertEquals(1, a.getAuthors().get(1).getAuthorIndex());
        assertSame(owner, a.getOwner());
        assertSame(petr, a.getAuthors().get(1).getPerson());
        assertEquals("ПрИн-467", a.getGroupName());
        assertEquals(FundingType.BUDGET, a.getFundingType());
        assertEquals("ПрИн-468", a.getAuthors().get(1).getGroupName());
        assertEquals(FundingType.CONTRACT, a.getAuthors().get(1).getFundingType());

        assertTrue(a.isAuthor(owner));
        assertTrue(a.isAuthor(petr));
        assertFalse(a.isAuthor(stranger));
        assertEquals(1, a.authorOf(petr).get().getAuthorIndex());
        assertTrue(a.authorOf(stranger).isEmpty());

        assertThrows(UnsupportedOperationException.class, () -> a.getAuthors().clear());
        assertEquals(2, a.getAuthors().size());
    }

    @Test
    void test_update_changesOnlyCoAuthorData() {
        Participant owner = participant("Иванов Иван", "ivan@yandex.ru", "88000000000");
        Participant petr = participant("Петров Пётр", "petr@yandex.ru", "89990000000");
        Application withCoAuthor = new Application(owner, petr, DIRECTION,
                request("Тема", List.of(FIRST), coAuthor("petr@yandex.ru")));
        Application alone = new Application(owner, DIRECTION, request("Одна", List.of(FIRST), null));

        withCoAuthor.update(
                request("Тема", List.of(FIRST), new CoAuthorRequest("anna@yandex.ru", "ПрИн-466", FundingType.BUDGET)));

        assertEquals(2, withCoAuthor.getAuthors().size());
        assertSame(petr, withCoAuthor.getAuthors().get(1).getPerson());
        assertEquals("ПрИн-466", withCoAuthor.getAuthors().get(1).getGroupName());
        assertEquals(FundingType.BUDGET, withCoAuthor.getAuthors().get(1).getFundingType());

        // соавтора после подачи добавить нельзя
        alone.update(request("Одна", List.of(FIRST), coAuthor("petr@yandex.ru")));
        assertEquals(1, alone.getAuthors().size());
        assertSame(owner, alone.getOwner());
    }

    @Test
    void test_responses() {
        Participant owner = participant("Иванов Иван Иванович", "ivan@yandex.ru", "88000000000");
        Participant petr = participant("Петров Пётр Петрович", "petr@yandex.ru", "89990000000");
        Application a = new Application(owner, petr, DIRECTION,
                request("Тема", List.of(FIRST, SECOND), coAuthor("petr@yandex.ru")));

        ApplicationResponse response = ApplicationResponse.from(a);

        assertEquals(a.getId(), response.id());
        assertEquals("Тема", response.title());
        assertEquals(ApplicationStatus.SUBMITTED, response.status());
        assertEquals(7, response.directionId());
        assertEquals(2, response.authors().size());
        assertEquals("ivan@yandex.ru", response.authors().get(0).person().email());
        assertEquals("ПрИн-467", response.authors().get(0).groupName());
        assertEquals("petr@yandex.ru", response.authors().get(1).person().email());
        assertEquals("ПрИн-468", response.authors().get(1).groupName());
        assertEquals(2, response.supervisors().size());
        assertEquals("Петрова Анна Сергеевна", response.supervisors().get(1).fullName());

        String expertText = ExpertApplicationResponse.from(a).toString();
        assertTrue(expertText.contains("Тема"));
        assertFalse(expertText.contains("Иванов"));
        assertFalse(expertText.contains("Петров"));
        assertFalse(expertText.contains("ivan@yandex.ru"));
        assertFalse(expertText.contains("petr@yandex.ru"));
        assertFalse(expertText.contains("Дроботов"));
        assertFalse(expertText.contains("ВолгГУ"));
        assertFalse(expertText.contains("ПрИн-468"));
    }
}
