-- payment attempts per order
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    method VARCHAR(20) NOT NULL CHECK (method IN ('CARD', 'CASH_ON_DELIVERY', 'BANK_TRANSFER')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'AUTHORISED', 'CAPTURED', 'FAILED')),
    provider_reference VARCHAR(255),
    amount BIGINT NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- the primary key here is the concurrency mechanism: a duplicate insert means a replayed order placement
CREATE TABLE idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    request_fingerprint VARCHAR(255) NOT NULL,
    order_id UUID REFERENCES orders(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
