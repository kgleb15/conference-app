package com.kulikov.conference_app.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class SecurityTests {

    @Autowired
    private WebApplicationContext _context; // контекст приложения

    private MockMvc _mvc; // виртуальный http сервер

    @BeforeEach
    void setUp() {
        // Создаем mock и добавляем фильтры безопасности
        _mvc = MockMvcBuilders.webAppContextSetup(_context).apply(springSecurity()).build();
    }

    @Test
    void test_noLogin_unauthorized() throws Exception {
        _mvc.perform(get("/applications")).andExpect(status().isUnauthorized());
    }

    @Test
    void test_organizerLogin() throws Exception {
        // верный логин и пароль
        _mvc.perform(get("/organizer/applications").with(httpBasic("organizer@example.com", "organizer123")))
                .andExpect(status().isOk());

        // вошёл но в раздел эксперта (403)
        _mvc.perform(get("/expert/applications").with(httpBasic("organizer@example.com", "organizer123")))
                .andExpect(status().isForbidden());

        // неверный пароль (401)
        _mvc.perform(get("/organizer/applications").with(httpBasic("organizer@example.com", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
