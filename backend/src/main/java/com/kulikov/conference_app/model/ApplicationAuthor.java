package com.kulikov.conference_app.model;

import java.time.Instant;
import java.util.UUID;

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

    private Instant acknowledgedAt;

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

    /**
     * Отмечает, что автор ознакомлен
     */
    public void acknowledge(Instant now) {
        if (acknowledgedAt == null) {
            acknowledgedAt = now;
        }
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

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }
}
