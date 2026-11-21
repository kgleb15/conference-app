ALTER TABLE application ADD COLUMN supervisor1_full_name varchar(255);
ALTER TABLE application ADD COLUMN supervisor1_workplace varchar(255);
ALTER TABLE application ADD COLUMN supervisor2_full_name varchar(255);
ALTER TABLE application ADD COLUMN supervisor2_workplace varchar(255);

UPDATE application SET supervisor1_full_name = 'не указан', supervisor1_workplace = 'не указано';

ALTER TABLE application ALTER COLUMN supervisor1_full_name SET NOT NULL;
ALTER TABLE application ALTER COLUMN supervisor1_workplace SET NOT NULL;
