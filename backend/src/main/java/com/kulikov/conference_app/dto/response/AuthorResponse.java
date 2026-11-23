package com.kulikov.conference_app.dto.response;

import com.kulikov.conference_app.model.application.ApplicationAuthor;
import com.kulikov.conference_app.model.application.FundingType;

public record AuthorResponse(PersonResponse person, String groupName, FundingType fundingType) {
    public static AuthorResponse from(ApplicationAuthor a) {
        return new AuthorResponse(PersonResponse.from(a.getPerson()), a.getGroupName(),
                a.getFundingType());
    }
}
