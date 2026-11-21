package com.kulikov.conference_app.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class ConferencePropertiesTests {

    private final Validator _validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_t2BeforeT1() {
        ConferenceProperties p = new ConferenceProperties();
        p.setDeadlineT1(Instant.parse("2026-12-31T23:59:59Z"));
        p.setDeadlineT2(Instant.parse("2026-12-30T00:00:00Z"));
        assertFalse(_validator.validate(p).isEmpty());
    }

    @Test
    void test_t2EqualsT1() {
        ConferenceProperties p = new ConferenceProperties();
        p.setDeadlineT1(Instant.parse("2026-12-31T23:59:59Z"));
        p.setDeadlineT2(Instant.parse("2026-12-31T23:59:59Z"));
        assertTrue(_validator.validate(p).isEmpty());
    }

    @Test
    void test_t2AfterT1() {
        ConferenceProperties p = new ConferenceProperties();
        p.setDeadlineT1(Instant.parse("2026-12-30T23:59:59Z"));
        p.setDeadlineT2(Instant.parse("2026-12-31T23:59:59Z"));
        assertTrue(_validator.validate(p).isEmpty());
    }
}
