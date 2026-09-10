-- adds cancellation and refund tracking fields, and REFUNDED to the payment status domain
ALTER TABLE orders
    ADD COLUMN cancellation_reason TEXT,
    ADD COLUMN refunded_amount BIGINT;

ALTER TABLE orders
    DROP CONSTRAINT orders_payment_status_check,
    ADD CONSTRAINT orders_payment_status_check
        CHECK (payment_status IN ('PENDING', 'AUTHORISED', 'CAPTURED', 'FAILED', 'REFUNDED'));
