create table if not exists events (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    venue text not null,
    city text not null,
    description text,
    start_time timestamptz not null,
    end_time   timestamptz not null,
    price_min numeric(10,2),
    price_max numeric(10,2),
    total_seats int not null,
    available_seats int not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
    );

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS outbox_events (
                                             id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate        text        NOT NULL,
    aggregate_id     uuid        NOT NULL,
    type             text        NOT NULL,
    payload          jsonb       NOT NULL,
    status           text        NOT NULL DEFAULT 'PENDING',
    attempts         int         NOT NULL DEFAULT 0,
    next_attempt_at  timestamptz NOT NULL DEFAULT now(),
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_events (status, next_attempt_at);