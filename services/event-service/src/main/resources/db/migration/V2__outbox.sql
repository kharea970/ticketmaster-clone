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
