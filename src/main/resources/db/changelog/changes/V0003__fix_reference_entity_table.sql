--liquibase formatted sql

--changeset ivt:V0003_a
ALTER TABLE public.reference_entity RENAME COLUMN configuredid TO configured_id;
--rollback ALTER TABLE public.reference_entity RENAME COLUMN configured_id TO configuredid;
