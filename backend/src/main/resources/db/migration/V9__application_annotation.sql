ALTER TABLE application ADD COLUMN annotation text;

UPDATE application SET annotation = 'отсутствует';

ALTER TABLE application ALTER COLUMN annotation SET NOT NULL;
