ALTER TABLE application ADD COLUMN group_name varchar(255);
ALTER TABLE application ADD COLUMN applicant_category varchar(255);
ALTER TABLE application ADD COLUMN funding_type varchar(255);

UPDATE application
SET group_name = 'ПрИн-467',
    applicant_category = 'STUDENT',
    funding_type = 'BUDGET';

ALTER TABLE application ALTER COLUMN group_name SET NOT NULL;
ALTER TABLE application ALTER COLUMN applicant_category SET NOT NULL;
ALTER TABLE application ALTER COLUMN funding_type SET NOT NULL;

ALTER TABLE person DROP COLUMN group_name;