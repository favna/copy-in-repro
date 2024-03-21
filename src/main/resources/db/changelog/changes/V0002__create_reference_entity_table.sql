--liquibase formatted sql

--changeset ivt:V0002_a
CREATE TABLE public.reference_entity
(
    id           BIGSERIAL,
    configuredId VARCHAR(255) NOT NULL,
    PRIMARY KEY (configuredId)
);

CREATE INDEX reference_id_idx ON public.reference_entity (id);
--rollback DROP TABLE public.reference_entity;
