-- adds E_WALLET to the payment method domain
ALTER TABLE orders
    DROP CONSTRAINT orders_payment_method_check,
    ADD CONSTRAINT orders_payment_method_check
        CHECK (payment_method IN ('CARD', 'CASH_ON_DELIVERY', 'BANK_TRANSFER', 'E_WALLET'));

ALTER TABLE payments
    DROP CONSTRAINT payments_method_check,
    ADD CONSTRAINT payments_method_check
        CHECK (method IN ('CARD', 'CASH_ON_DELIVERY', 'BANK_TRANSFER', 'E_WALLET'));
