package com.kulikov.conference_app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationRequest;
import com.kulikov.conference_app.model.ApplicationResponse;
import com.kulikov.conference_app.services.ApplicationService;
import com.kulikov.conference_app.services.ServiceListResult;
import com.kulikov.conference_app.services.ServiceResult;

import jakarta.validation.Valid;

/**
 * HTTP слой для работы с заявками
 */
@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService _applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this._applicationService = applicationService;
    }

    /**
     * Возвращает список заявок текущего участника
     *
     * @return 200 со списком своих заявок; 401 если запрос не идентифицирован
     */
    @GetMapping
    ResponseEntity<List<ApplicationResponse>> getApplications() {
        return toListResponse(_applicationService.findAll());
    }

    /**
     * Возвращает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return 200 с заявкой или 404, если не найдена, 401 если запрос не идентифицирован
     */
    @GetMapping("/{id}")
    ResponseEntity<ApplicationResponse> getApplication(@PathVariable String id) {
        return toResponse(_applicationService.get(id));
    }

    /**
     * Создает новую заявку
     * 
     * @param request - данные новой заявки
     * @return 201 с созданной заявкой; 403 если дата некорректна; 
     *         400 если некорректны поля запроса; 401 если запрос не идентифицирован
     */
    @PostMapping
    ResponseEntity<ApplicationResponse> submit(@Valid @RequestBody ApplicationRequest request) {
        return toResponse(_applicationService.submit(request));
    }

    /**
     * Обновляет заявку или создает новую
     * 
     * @param id      - идентификатор заявки
     * @param request - новые данные заявки
     * @return 201 если создана новая; 409 если отозвана; 200 если успешно обновлена;
     *         403 если дата некорректна; 400 если некорректны поля запроса; 401 если участник не найден
     */
    @PutMapping("/{id}")
    ResponseEntity<ApplicationResponse> edit(@PathVariable String id, @Valid @RequestBody ApplicationRequest request) {
        return toResponse(_applicationService.edit(id, request));
    }

    /**
     * Отзывает заявку по id
     * 
     * @param id - идентификатор заявки
     * @return 200 с отозванной заявкой если успешно отозвана; 404 если не найдена;
     *         403 если дата некорректна; 401 если участник не найден
     */
    @PatchMapping("/{id}/withdraw")
    ResponseEntity<ApplicationResponse> withdraw(@PathVariable String id) {
        return toResponse(_applicationService.withdraw(id));
    }

    /**
     * Переводит ServiceStatus в HTTP ответ (для одной заявки)
     *
     * @param result - результат операции сервиса
     * @return HTTP ответ
     */
    private ResponseEntity<ApplicationResponse> toResponse(ServiceResult result) {
        HttpStatus httpStatus;
        switch (result.status()) {
            case OK: httpStatus = HttpStatus.OK; break;
            case CREATED: httpStatus = HttpStatus.CREATED; break;
            case NOT_FOUND: httpStatus = HttpStatus.NOT_FOUND; break;
            case DEADLINE_PASSED: httpStatus = HttpStatus.FORBIDDEN; break;
            case WITHDRAWN: httpStatus = HttpStatus.CONFLICT; break;
            case UNAUTHORIZED: httpStatus = HttpStatus.UNAUTHORIZED; break;
            default: httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ApplicationResponse body = result.application() == null
                ? null : ApplicationResponse.from(result.application());
        return new ResponseEntity<>(body, httpStatus);
    }

    /**
     * Переводит ServiceListResult в HTTP ответ для списка заявок
     *
     * @param result - результат операции сервиса
     * @return HTTP ответ со списком или без тела
     */
    private ResponseEntity<List<ApplicationResponse>> toListResponse(ServiceListResult result) {
        HttpStatus httpStatus;
        switch (result.status()) {
            case OK: httpStatus = HttpStatus.OK; break;
            case UNAUTHORIZED: httpStatus = HttpStatus.UNAUTHORIZED; break;
            default: httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        List<ApplicationResponse> body = new ArrayList<>();
        for (Application app : result.applications()) {
            body.add(ApplicationResponse.from(app));
        }
        return new ResponseEntity<>(body, httpStatus);
    }
}
