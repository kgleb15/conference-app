ALTER TABLE person ADD COLUMN direction_id integer;
ALTER TABLE person ADD CONSTRAINT fk_person_direction FOREIGN KEY (direction_id) REFERENCES direction (id);

UPDATE person SET direction_id = 7 WHERE role IN ('ORGANIZER', 'EXPERT');
UPDATE person SET phone = NULL WHERE role <> 'PARTICIPANT';
