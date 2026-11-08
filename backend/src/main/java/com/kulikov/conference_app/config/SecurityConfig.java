package com.kulikov.conference_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Настройки безопасности
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Собирает цепочку фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var unauthorized = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
        http
                // отключена CSRF, т.к. нет куки и сессий
                .csrf(csrf -> csrf.disable())
                // каждый запрос проверяется заново.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 401 без заголовка WWW-Authenticate
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized))
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .authorizeHttpRequests(auth -> auth
                        // Адрес /error для показа ошибок
                        .requestMatchers("/error").permitAll()
                        // Регистрация участника открыта всем
                        .requestMatchers(HttpMethod.POST, "/persons").permitAll()
                        // Организаторов и экспертов создаёт только организатор
                        .requestMatchers("/persons/organizer", "/persons/expert").hasRole("ORGANIZER")
                        // остальное - только для вошедших
                        .anyRequest().authenticated());
        return http.build();
    }
}
