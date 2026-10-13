package com.kulikov.conference_app.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kulikov.conference_app.model.Application;
import com.kulikov.conference_app.model.ApplicationResponse;
import com.kulikov.conference_app.model.ApplicationStatus;
import com.kulikov.conference_app.services.ApplicationService;
import com.kulikov.conference_app.services.ServiceListResult;

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
     * @param status - фильтр по статусу (SUBMITTED, WITHDRAWN) или null
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
}
