--liquibase formatted sql

--changeset ivt:V0001_a
CREATE TABLE public.test
(
    id        BIGSERIAL,
    subject   VARCHAR(255) NOT NULL,
    predicate VARCHAR(255) NOT NULL,
    object    TEXT         NOT NULL,
    PRIMARY KEY (subject, predicate, object)
);
--rollback DROP TABLE public.test;
