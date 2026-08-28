-- launch promotion: 10% off, no minimum spend, open-ended
INSERT INTO promotions (code, type, value, minimum_subtotal_amount, is_active, starts_at, ends_at)
VALUES ('SPACE10', 'PERCENTAGE', 10, NULL, true, NULL, NULL);
