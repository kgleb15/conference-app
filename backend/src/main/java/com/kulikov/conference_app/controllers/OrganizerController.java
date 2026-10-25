package com.kulikov.conference_app.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationResponse;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.model.ModerationRequest;
import com.kulikov.conference_app.services.ApplicationService;
import com.kulikov.conference_app.services.ServiceListResult;
import com.kulikov.conference_app.services.ServiceResult;

import jakarta.validation.Valid;

/**
 * HTTP слой для организатора
 */
@RestController
@RequestMapping("/organizer/applications")
public class OrganizerController {

    private final ApplicationService _applicationService;

    public OrganizerController(ApplicationService applicationService) {
        this._applicationService = applicationService;
    }

    /**
     * Возвращает все заявки (с фильтром по статусу)
     *
     * @param status - фильтр по статусу (SUBMITTED, WITHDRAWN, APPROVED, REJECTED,
     *               REVISION_REQUESTED) или null
     * @return 200 со списком заявок
     */
    @GetMapping
    ResponseEntity<List<ApplicationResponse>> getApplications(@RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Boolean edited) {

        ServiceListResult result = _applicationService.findAllForOrganizer(status, edited);

        HttpStatus httpStatus;
        switch (result.status()) {
            case OK:
                httpStatus = HttpStatus.OK;
                break;
            case UNAUTHORIZED:
                httpStatus = HttpStatus.UNAUTHORIZED;
                break;
            case FORBIDDEN:
                httpStatus = HttpStatus.FORBIDDEN;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        List<ApplicationResponse> body = null;
        if (result.applications() != null) {
            body = new ArrayList<>();
            for (Application application : result.applications()) {
                body.add(ApplicationResponse.from(application));
            }
        }

        return new ResponseEntity<>(body, httpStatus);
    }

    /**
     * Утверждает заявку
     *
     * @param id - идентификатор заявки
     * @return 200 с утверждённой заявкой; 404 если такой заявки нет; 409 если по
     *         заявке
     *         уже нельзя принять решение; 401 если запрос не идентифицирован;
     *         403 если пользователь не организатор
     */
    @PatchMapping("/{id}/approve")
    ResponseEntity<ApplicationResponse> approve(@PathVariable String id) {
        return toResponse(_applicationService.approve(id, null));
    }

    /**
     * Отклоняет заявку
     *
     * @param id      - идентификатор заявки
     * @param request - комментарий организатора
     * @return 200 с отклонённой заявкой; 400 если нет комментария; 404 если такой
     *         заявки нет;
     *         409 если по заявке уже нельзя принять решение; 401 если запрос не
     *         идентифицирован;
     *         403 если пользователь не организатор
     */
    @PatchMapping("/{id}/reject")
    ResponseEntity<ApplicationResponse> reject(@PathVariable String id,
            @Valid @RequestBody ModerationRequest request) {
        return toResponse(_applicationService.reject(id, request.comment()));
    }

    /**
     * Отправляет заявку на доработку
     *
     * @param id      - идентификатор заявки
     * @param request - комментарий организатора, что нужно исправить
     * @return 200 с заявкой на доработке; 400 если нет комментария; 404 если такой
     *         заявки нет;
     *         409 если по заявке уже нельзя принять решение; 401 если запрос не
     *         идентифицирован;
     *         403 если пользователь не организатор
     */
    @PatchMapping("/{id}/request-revision")
    ResponseEntity<ApplicationResponse> requestRevision(@PathVariable String id,
            @Valid @RequestBody ModerationRequest request) {
        return toResponse(_applicationService.requestRevision(id, request.comment()));
    }

    /**
     * Переводит ServiceStatus в HTTP ответ
     *
     * @param result - результат операции сервиса
     * @return HTTP ответ
     */
    private ResponseEntity<ApplicationResponse> toResponse(ServiceResult result) {
        HttpStatus httpStatus;
        switch (result.status()) {
            case OK:
                httpStatus = HttpStatus.OK;
                break;
            case NOT_FOUND:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case INVALID_STATE:
                httpStatus = HttpStatus.CONFLICT;
                break;
            case UNAUTHORIZED:
                httpStatus = HttpStatus.UNAUTHORIZED;
                break;
            case FORBIDDEN:
                httpStatus = HttpStatus.FORBIDDEN;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ApplicationResponse body = result.application() == null
                ? null
                : ApplicationResponse.from(result.application());
        return new ResponseEntity<>(body, httpStatus);
    }
}
