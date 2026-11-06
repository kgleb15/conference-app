DELETE FROM application;
DELETE FROM person;

ALTER TABLE person ADD COLUMN password_hash varchar(255) NOT NULL;
ALTER TABLE person ALTER COLUMN email SET NOT NULL;
CREATE UNIQUE INDEX uq_person_email ON person (lower(email));
