ALTER TABLE application ADD COLUMN transfer_target_id integer;
ALTER TABLE application ADD CONSTRAINT fk_application_transfer_target
    FOREIGN KEY (transfer_target_id) REFERENCES direction (id);
