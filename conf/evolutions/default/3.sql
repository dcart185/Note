# --- !Ups

ALTER TABLE `Person`
ADD `master_key` VARCHAR(255);

# --- !Downs

ALTER TABLE `Person`
DROP COLUMN `master_key`;


# --- !Ups
DROP PROCEDURE IF EXISTS SP_INSERT_PERSON;

CREATE PROCEDURE SP_INSERT_PERSON(IN in_first_name VARCHAR(128), IN in_last_name VARCHAR(128), IN in_email VARCHAR(128),
    IN in_password VARCHAR(255), IN in_master_key VARCHAR(255), OUT insert_id BIGINT(20))

    BEGIN
        INSERT INTO Person(first_name,last_name,email,password,master_key)
        VALUES(in_first_name,in_last_name,in_email,in_password,in_master_key);;
        SET insert_id = LAST_INSERT_ID();;
    END;

# --- !Downs
DROP PROCEDURE IF EXISTS SP_INSERT_PERSON;

CREATE PROCEDURE SP_INSERT_PERSON(IN in_first_name VARCHAR(128), IN in_last_name VARCHAR(128), IN in_email VARCHAR(128),
    IN in_password VARCHAR(255), OUT insert_id BIGINT(20))

    BEGIN
        INSERT INTO Person(first_name,last_name,email,password)
        VALUES(in_first_name,in_last_name,in_email,in_password);;
        SET insert_id = LAST_INSERT_ID();;
    END;