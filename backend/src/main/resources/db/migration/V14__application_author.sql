CREATE TABLE application_author (
    id              varchar(255) NOT NULL PRIMARY KEY,
    application_id  varchar(255) NOT NULL REFERENCES application (id),
    person_id       varchar(255) NOT NULL REFERENCES person (id),
    author_index    integer NOT NULL,
    group_name      varchar(255) NOT NULL,
    funding_type    varchar(255) NOT NULL,
    acknowledged_at timestamp(6) with time zone,
    CONSTRAINT uq_author_person UNIQUE (application_id, person_id),
    CONSTRAINT uq_author_index UNIQUE (application_id, author_index)
);

-- существующие владельцы становятся первыми авторами
INSERT INTO application_author (id, application_id, person_id, author_index, group_name, funding_type)
SELECT gen_random_uuid()::text, id, owner_id, 0, group_name, funding_type FROM application;

ALTER TABLE application DROP COLUMN owner_id, DROP COLUMN group_name, DROP COLUMN funding_type;
