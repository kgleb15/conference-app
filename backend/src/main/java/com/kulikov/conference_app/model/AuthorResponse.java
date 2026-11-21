package com.kulikov.conference_app.model;

import java.time.Instant;

public record AuthorResponse(PersonResponse person, String groupName, FundingType fundingType) {
    public static AuthorResponse from(ApplicationAuthor a) {
        return new AuthorResponse(PersonResponse.from(a.getPerson()), a.getGroupName(),
                a.getFundingType());
    }
}
