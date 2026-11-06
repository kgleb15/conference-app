package com.kulikov.conference_app.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ExpertApplicationResponse;
import com.kulikov.conference_app.services.ApplicationService;
import com.kulikov.conference_app.services.ServiceListResult;
import com.kulikov.conference_app.services.ServiceResult;
import com.kulikov.conference_app.services.ServiceStatus;

/**
 * HTTP слой для члена экспертной комиссии
 */
@RestController
@RequestMapping("/expert/applications")
public class ExpertController {

    private final ApplicationService _applicationService;

    public ExpertController(ApplicationService applicationService) {
        this._applicationService = applicationService;
    }

    /**
     * Возвращает утверждённые заявки без данных об авторах
     *
     * @return 200 со списком; 401 если запрос не идентифицирован;
     *         403 если пользователь не эксперт
     */
    @GetMapping
    ResponseEntity<List<ExpertApplicationResponse>> getApplications() {
        ServiceListResult result = _applicationService.findAllForExpert();

        List<ExpertApplicationResponse> body = null;
        if (result.applications() != null) {
            body = new ArrayList<>();
            for (Application application : result.applications()) {
                body.add(ExpertApplicationResponse.from(application));
            }
        }
        return new ResponseEntity<>(body, toHttpStatus(result.status()));
    }

    /**
     * Возвращает утверждённую заявку без данных об авторе
     *
     * @param id - идентификатор заявки
     * @return 200 с заявкой; 404 если не найдена или не утверждена;
     *         401 если запрос не идентифицирован; 403 если пользователь не эксперт
     */
    @GetMapping("/{id}")
    ResponseEntity<ExpertApplicationResponse> getApplication(@PathVariable String id) {
        ServiceResult result = _applicationService.getForExpert(id);

        ExpertApplicationResponse body = result.application() == null
                ? null
                : ExpertApplicationResponse.from(result.application());
        return new ResponseEntity<>(body, toHttpStatus(result.status()));
    }

    private HttpStatus toHttpStatus(ServiceStatus status) {
        switch (status) {
            case OK:
                return HttpStatus.OK;
            case NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}