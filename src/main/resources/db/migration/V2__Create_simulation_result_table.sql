CREATE TABLE simulation (
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    batch_id UUID NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount_requested DECIMAL(15,2) NOT NULL,
    birthdate DATE NOT NULL,
    installments INTEGER NOT NULL,
    total_amount DECIMAL(15,2) NULL,
    installment_amount DECIMAL(15,2) NULL,
    total_fee DECIMAL(15,2) NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_simulation_batch FOREIGN KEY (batch_id) REFERENCES batch_simulation(id) ON DELETE CASCADE
);
