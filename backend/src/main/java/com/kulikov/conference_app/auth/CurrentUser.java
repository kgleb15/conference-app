package com.kulikov.conference_app.auth;

import java.util.Optional;

/**
 * Определяет, от имени какого участника пришёл запрос
 */
public interface CurrentUser {

    /**
     * Возвращает id участника, от имени которого пришёл запрос
     *
     * @return id участника или empty, если запрос не идентифицирован
     */
    Optional<String> personId();
}
