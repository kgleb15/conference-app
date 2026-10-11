package com.kulikov.conference_app.repositories;

import java.util.List;
import java.util.Optional;

import com.kulikov.conference_app.model.Application;

/**
 * Интерфейс хранилища заявок
 */
public interface ApplicationRepository {

    /**
     * Возвращает все заявки.
     *
     * @return список заявок (пустой, если заявок нет)
     */
    List<Application> findAll();

    /**
     * Возвращает заявку по id.
     *
     * @param id - идентификатор заявки
     * @return найденная заявка или empty, если не найдена
     */
    Optional<Application> findById(String id);

    /**
     * Сохраняет заявку: создаёт новую или перезаписывает существующую по id
     *
     * @param application - заявка для сохранения
     * @return сохранённая заявка
     */
    Application save(Application application);
}
