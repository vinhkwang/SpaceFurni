-- single-row platform settings for admin-configurable delivery fees and the low-stock threshold
CREATE TABLE platform_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    free_delivery_threshold_amount BIGINT NOT NULL,
    standard_delivery_fee_amount BIGINT NOT NULL,
    next_day_delivery_fee_amount BIGINT NOT NULL,
    low_stock_threshold_units INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO platform_settings (free_delivery_threshold_amount, standard_delivery_fee_amount,
    next_day_delivery_fee_amount, low_stock_threshold_units)
VALUES (10000000, 300000, 300000, 6);
