ALTER TABLE application ADD COLUMN owner_id varchar(255) NOT NULL;

ALTER TABLE application ADD CONSTRAINT fk_application_owner FOREIGN KEY (owner_id) REFERENCES person (id);

ALTER TABLE application DROP COLUMN author_full_name, DROP COLUMN author_group,
        DROP COLUMN author_email, DROP COLUMN author_phone;
