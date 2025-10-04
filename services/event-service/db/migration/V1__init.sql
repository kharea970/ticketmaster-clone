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

