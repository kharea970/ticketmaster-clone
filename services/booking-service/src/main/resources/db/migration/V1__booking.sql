create table if not exists bookings (
    id               uuid primary key,
    event_id         uuid not null,
    user_id          varchar(100) not null,
    status           varchar(20) not null,              -- PENDING, CONFIRMED, CANCELLED
    total_amount     numeric(10,2) not null default 0,
    created_at       timestamptz not null default now(),
    updated_at       timestamptz not null default now(),
    version          bigint not null default 0
    );

create table if not exists booking_items (
    id           uuid primary key,
    booking_id   uuid not null references bookings(id) on delete cascade,
    seat_no      integer not null,
    price        numeric(10,2) not null
    );

create index if not exists idx_booking_items_booking on booking_items(booking_id);
