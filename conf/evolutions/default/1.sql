# --- !Ups

CREATE TABLE `Person`(
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `first_name` VARCHAR(128) NOT NULL,
    `last_name` VARCHAR(128) NOT NULL,
    `email` VARCHAR(128) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    PRIMARY KEY(`id`),
    UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE `Person`;


# --- !Ups

CREATE PROCEDURE SP_INSERT_PERSON(IN in_first_name VARCHAR(128), IN in_last_name VARCHAR(128), IN in_email VARCHAR(128),
    IN in_password VARCHAR(255), OUT insert_id BIGINT(20))

    BEGIN
        INSERT INTO Person(first_name,last_name,email,password)
        VALUES(in_first_name,in_last_name,in_email,in_password);;
        SET insert_id = LAST_INSERT_ID();;
    END;

# --- !Downs
DROP PROCEDURE SP_INSERT_PERSON;

# --- !Ups
CREATE PROCEDURE SP_GET_PERSON(IN in_id BIGINT(20))
    BEGIN
        SELECT *
        FROM Person
        WHERE id = in_id;;
    END;

# --- !Downs
DROP PROCEDURE SP_GET_PERSON;


# --- !Ups
CREATE PROCEDURE SP_GET_PERSON_BY_EMAIL(IN in_email VARCHAR(128))
    BEGIN
        SELECT *
        FROM Person
        WHERE email = in_email;;
    END;

# --- !Downs
DROP PROCEDURE SP_GET_PERSON_BY_EMAIL;








