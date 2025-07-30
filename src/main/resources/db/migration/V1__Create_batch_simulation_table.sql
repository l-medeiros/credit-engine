CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE batch_status AS ENUM (
    'PENDING',
    'PROCESSING', 
    'COMPLETED'
);

CREATE TABLE batch_simulation (
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    status batch_status NOT NULL DEFAULT 'PENDING',
    total_simulations INTEGER NOT NULL DEFAULT 0,
    completed_simulations INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

