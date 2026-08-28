-- promotion codes, keyed by their own uppercase-normalised code
CREATE TABLE promotions (
    code VARCHAR(20) PRIMARY KEY CHECK (code = upper(code)),
    type VARCHAR(20) NOT NULL CHECK (type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    value BIGINT NOT NULL,
    minimum_subtotal_amount BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ
);
