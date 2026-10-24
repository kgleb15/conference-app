ALTER TABLE application ADD COLUMN moderation_comment text;
ALTER TABLE application ADD COLUMN decided_at timestamp(6) with time zone;
ALTER TABLE application ADD COLUMN decided_by_id varchar(255);

ALTER TABLE application ADD CONSTRAINT fk_application_decided_by FOREIGN KEY (decided_by_id) REFERENCES person (id);
