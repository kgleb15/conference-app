package com.kulikov.conference_app.dto.response;

import com.kulikov.conference_app.model.person.Participant;
import com.kulikov.conference_app.model.person.Person;
import com.kulikov.conference_app.model.person.PersonRole;
import com.kulikov.conference_app.model.person.Staff;

public record PersonResponse(String id, String fullName,
                             String email, String phone, PersonRole role, Integer directionId) {

    /**
     * Собирает ответ из сущности Person
     */
    public static PersonResponse from(Person p) {
        String phone = p instanceof Participant participant ? participant.getPhone() : null;
        Integer directionId = p instanceof Staff staff ? staff.getDirection().getId() : null;
        return new PersonResponse(p.getId(), p.getFullName(), p.getEmail(), phone, p.getRole(), directionId);
    }
}
