package com.kulikov.conference_app.controllers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Сквозные тесты HTTP API (хранилище в памяти)
 */
@SpringBootTest(properties = { "conference.deadlineT1=2099-01-01T00:00:00Z",
        "conference.deadlineT2=2099-02-01T00:00:00Z" })
public class ApiFlowTests {

    private static final String PASSWORD = "password123";
    private static final String ORGANIZER = "organizer@example.com";
    private static final String ORGANIZER_PASSWORD = "organizer123";
    private static final String ONE_SUPERVISOR = "[{\"fullName\":\"Дроботов Алексей\",\"workplace\":\"ВолгГТУ\"}]";

    @Autowired
    private WebApplicationContext _context;

    private MockMvc _mvc;

    @BeforeEach
    void setUp() {
        _mvc = MockMvcBuilders.webAppContextSetup(_context).apply(springSecurity()).build();
    }

    // ---------------- вспомогательные методы ----------------

    private String newEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@test.ru";
    }

    private String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return _mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions postJson(String url, String json, String email, String password) throws Exception {
        return _mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json)
                .with(httpBasic(email, password)));
    }

    private ResultActions patchJson(String url, String json, String email, String password) throws Exception {
        return _mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(json)
                .with(httpBasic(email, password)));
    }

    private ResultActions getAs(String url, String email, String password) throws Exception {
        return _mvc.perform(get(url).with(httpBasic(email, password)));
    }

    private void registerParticipant(String email, String phone) throws Exception {
        String json = """
                {"fullName":"Иванов Иван Иванович","email":"%s","phone":"%s","password":"%s"}
                """.formatted(email, phone, PASSWORD);
        postJson("/persons", json).andExpect(status().isCreated());
    }

    private String applicationJson(String title, int directionId, String supervisors, String coAuthor) {
        return """
                {"title":"%s","annotation":"Аннотация","thesisText":"Текст тезисов","groupName":"ПрИн-467",
                 "applicantCategory":"STUDENT","fundingType":"BUDGET","directionId":%d,
                 "supervisors":%s,"coAuthor":%s}
                """.formatted(title, directionId, supervisors, coAuthor);
    }

    /** Подаёт заявку от имени участника и возвращает её id */
    private String submit(String email, String title, int directionId) throws Exception {
        String response = body(postJson("/applications",
                applicationJson(title, directionId, ONE_SUPERVISOR, "null"), email, PASSWORD)
                .andExpect(status().isCreated()));
        return idOf(response);
    }

    private String idOf(String json) {
        Matcher m = Pattern.compile("\"id\":\"([^\"]+)\"").matcher(json);
        assertTrue(m.find());
        return m.group(1);
    }

    @Test
    void test_registration() throws Exception {
        String email = newEmail();
        String json = """
                {"fullName":"Иванов Иван Иванович","email":"%s","phone":"88000000000","password":"%s"}
                """.formatted(email, PASSWORD);

        String created = body(postJson("/persons", json).andExpect(status().isCreated()));

        assertTrue(created.contains(email));
        assertTrue(created.contains("PARTICIPANT"));
        assertFalse(created.contains(PASSWORD));
        assertFalse(created.toLowerCase().contains("hash"));

        // тот же email
        postJson("/persons", json.replace(email, email.toUpperCase())).andExpect(status().isConflict());
        // некорректные поля
        postJson("/persons", "{\"fullName\":\"\",\"email\":\"not-an-email\",\"phone\":\"\",\"password\":\"123\"}")
                .andExpect(status().isBadRequest());
        // созданный участник может войти
        getAs("/applications", email, PASSWORD).andExpect(status().isOk());
        getAs("/applications", email, "wrong-password").andExpect(status().isUnauthorized());
    }

    @Test
    void test_directions() throws Exception {
        String email = newEmail();
        registerParticipant(email, "88000000001");

        String response = body(getAs("/directions", email, PASSWORD).andExpect(status().isOk()));

        assertEquals(17, response.split("\"name\"", -1).length - 1);
        assertTrue(response.contains("Программно-информационное обеспечение"));
        assertTrue(response.contains("Цифровые технологии в урбанистике, архитектуре и строительстве"));
        _mvc.perform(get("/directions")).andExpect(status().isUnauthorized());
    }

    @Test
    void test_submit_validation() throws Exception {
        String email = newEmail();
        registerParticipant(email, "88000000002");
        String threeSupervisors = "[{\"fullName\":\"А\",\"workplace\":\"В\"},{\"fullName\":\"Б\",\"workplace\":\"В\"},"
                + "{\"fullName\":\"Г\",\"workplace\":\"В\"}]";

        postJson("/applications", applicationJson("", 7, ONE_SUPERVISOR, "null"), email, PASSWORD)
                .andExpect(status().isBadRequest());
        postJson("/applications", applicationJson("Тема", 7, "[]", "null"), email, PASSWORD)
                .andExpect(status().isBadRequest());
        postJson("/applications", applicationJson("Тема", 7, threeSupervisors, "null"), email, PASSWORD)
                .andExpect(status().isBadRequest());
        postJson("/applications", applicationJson("Тема", 99, ONE_SUPERVISOR, "null"), email, PASSWORD)
                .andExpect(status().isBadRequest());
        assertEquals("[]", body(getAs("/applications", email, PASSWORD).andExpect(status().isOk())));

        String created = body(postJson("/applications", applicationJson("Тема", 7, ONE_SUPERVISOR, "null"),
                email, PASSWORD).andExpect(status().isCreated()));
        assertTrue(created.contains("\"status\":\"SUBMITTED\""));
        assertTrue(created.contains("\"directionId\":7"));
        assertTrue(created.contains("Дроботов Алексей"));
        assertTrue(created.contains(email));
    }

    @Test
    void test_rolesAreSeparated() throws Exception {
        String email = newEmail();
        registerParticipant(email, "88000000003");

        getAs("/organizer/applications", email, PASSWORD).andExpect(status().isForbidden());
        getAs("/expert/applications", email, PASSWORD).andExpect(status().isForbidden());
        postJson("/persons/organizer", "{\"fullName\":\"Новый\",\"email\":\"" + newEmail()
                + "\",\"password\":\"password123\",\"directionId\":7}", email, PASSWORD)
                .andExpect(status().isForbidden());
        getAs("/organizer/applications", ORGANIZER, ORGANIZER_PASSWORD).andExpect(status().isOk());
        getAs("/expert/applications", ORGANIZER, ORGANIZER_PASSWORD).andExpect(status().isForbidden());
    }

    @Test
    void test_moderation() throws Exception {
        String email = newEmail();
        String phone = "89990001122";
        registerParticipant(email, phone);
        String title = "Тема-" + email.substring(0, 8);
        String id = submit(email, title, 7);

        String approved = body(patchJson("/organizer/applications/" + id + "/approve", "", ORGANIZER,
                ORGANIZER_PASSWORD).andExpect(status().isOk()));
        assertTrue(approved.contains("\"status\":\"APPROVED\""));
        patchJson("/organizer/applications/" + id + "/approve", "", ORGANIZER, ORGANIZER_PASSWORD)
                .andExpect(status().isConflict());

        String expertEmail = newEmail();
        postJson("/persons/expert", "{\"fullName\":\"Эксперт Эксп\",\"email\":\"" + expertEmail
                + "\",\"password\":\"password123\",\"directionId\":7}", ORGANIZER, ORGANIZER_PASSWORD)
                .andExpect(status().isCreated());
        String list = body(getAs("/expert/applications", expertEmail, PASSWORD).andExpect(status().isOk()));
        assertTrue(list.contains(title));
        assertFalse(list.contains("Вторая-" + email.substring(0, 8)));
        assertFalse(list.contains(email));
        assertFalse(list.contains(phone));
        assertFalse(list.contains("ПрИн-467"));
        assertFalse(list.contains("Дроботов"));
        assertFalse(list.contains("ВолгГТУ"));
    }

    @Test
    void test_transferToOtherDirection() throws Exception {
        String email = newEmail();
        registerParticipant(email, "88000000004");
        String id = submit(email, "Переводимая", 7);

        patchJson("/organizer/applications/" + id + "/transfer", "{\"directionId\":99}", ORGANIZER,
                ORGANIZER_PASSWORD).andExpect(status().isBadRequest());

        String moved = body(patchJson("/organizer/applications/" + id + "/transfer", "{\"directionId\":8}",
                ORGANIZER, ORGANIZER_PASSWORD).andExpect(status().isOk()));
        assertTrue(moved.contains("\"directionId\":8"));

        String organizerList = body(getAs("/organizer/applications", ORGANIZER, ORGANIZER_PASSWORD)
                .andExpect(status().isOk()));
        assertFalse(organizerList.contains(id));
        patchJson("/organizer/applications/" + id + "/approve", "", ORGANIZER, ORGANIZER_PASSWORD)
                .andExpect(status().isNotFound());
    }
}
