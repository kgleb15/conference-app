ALTER TABLE application ADD COLUMN direction_id integer;
UPDATE application SET direction_id = 7;
ALTER TABLE application ALTER COLUMN direction_id SET NOT NULL;
ALTER TABLE application ADD CONSTRAINT fk_application_direction FOREIGN KEY (direction_id) REFERENCES direction (id);
