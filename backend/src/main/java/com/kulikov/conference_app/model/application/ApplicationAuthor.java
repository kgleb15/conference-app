package com.kulikov.conference_app.model.application;

import java.util.UUID;

import com.kulikov.conference_app.model.person.Participant;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Автор заявки
 */
@Entity
public class ApplicationAuthor {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(optional = false)
    private Application application;

    @ManyToOne(optional = false)
    private Participant person;

    private int authorIndex;
    private String groupName;

    @Enumerated(EnumType.STRING)
    private FundingType fundingType;

    protected ApplicationAuthor() {
    }

    public ApplicationAuthor(Application application, Participant person, int authorIndex,
            String groupName, FundingType fundingType) {
        this.application = application;
        this.person = person;
        this.authorIndex = authorIndex;
        this.groupName = groupName;
        this.fundingType = fundingType;
    }

    /**
     * Меняет данные автора, относящиеся к этой работе
     */
    public void change(String groupName, FundingType fundingType) {
        this.groupName = groupName;
        this.fundingType = fundingType;
    }

    public Participant getPerson() {
        return person;
    }

    public int getAuthorIndex() {
        return authorIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    public FundingType getFundingType() {
        return fundingType;
    }
}
