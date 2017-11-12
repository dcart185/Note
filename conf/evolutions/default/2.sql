# --- !Ups

CREATE TABLE `Notes`(
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `person_id` BIGINT(20) NOT NULL,
    `subject` VARCHAR(128) NOT NULL,
    `note` VARCHAR(512) NOT NULL,
    PRIMARY KEY(`id`),
    FOREIGN KEY (person_id) REFERENCES Person(`id`),
    CONSTRAINT UC_PersonSubject UNIQUE (person_id,subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE `Notes`;

# --- !Ups
CREATE PROCEDURE SP_GET_NOTE(IN in_id BIGINT(20))
    BEGIN
        SELECT *
        FROM NOTES
        WHERE id = in_id;;
    END;

# --- !Downs
DROP PROCEDURE SP_GET_NOTE;


# --- !Ups
CREATE PROCEDURE SP_INSERT_NOTE(IN in_person_id BIGINT(20), IN in_subject VARCHAR(128), IN in_note VARCHAR(512),
     OUT insert_id BIGINT(20))

    BEGIN
        INSERT INTO Notes(person_id,subject,note)
        VALUES(in_person_id,in_subject,in_note);;
        SET insert_id = LAST_INSERT_ID();;
    END;

# --- !Downs
DROP PROCEDURE SP_INSERT_NOTE;


# --- !Ups
CREATE PROCEDURE SP_DELETE_NOTE(IN in_id BIGINT(20))
    BEGIN
        DELETE FROM NOTES WHERE id = in_id;;
    END;

# --- !Downs
DROP PROCEDURE SP_DELETE_NOTE;


# --- !Ups
CREATE PROCEDURE SP_UPDATE_NOTE(IN in_id BIGINT(20),IN in_person_id BIGINT(20), IN in_subject VARCHAR(128),
    IN in_note VARCHAR(512))
    BEGIN
        UPDATE NOTES
        SET NOTES.person_id = in_person_id, NOTES.subject = in_subject, NOTES.note = in_note
        WHERE NOTES.id = in_id;;
    END;

# --- !Downs
DROP PROCEDURE SP_UPDATE_NOTE;