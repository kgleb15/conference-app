ALTER TABLE application ADD COLUMN edit_count integer NOT NULL DEFAULT 0;
ALTER TABLE application ADD COLUMN last_edited_at timestamp(6) with time zone;
ALTER TABLE application ADD COLUMN withdrawn_at timestamp(6) with time zone;

UPDATE application SET withdrawn_at = updated_at WHERE status = 'WITHDRAWN';
