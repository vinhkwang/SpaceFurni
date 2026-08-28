-- lets a cart carry an applied promotion code through to pricing recomputation
ALTER TABLE carts ADD COLUMN promotion_code VARCHAR(20) REFERENCES promotions(code);
