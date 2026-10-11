CREATE TABLE application (
    id               varchar(255) NOT NULL PRIMARY KEY,
    created_at       timestamp(6) with time zone,
    updated_at       timestamp(6) with time zone,
    status           varchar(255),
    title            varchar(255),
    thesis_text      varchar(255),
    author_full_name varchar(255),
    author_group     varchar(255),
    author_email     varchar(255),
    author_phone     varchar(255)
);