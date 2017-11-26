# --- !Ups

ALTER TABLE `Person`
ADD `master_key` VARCHAR(255);

# --- !Downs

ALTER TABLE `Person`
DROP COLUMN `master_key`;