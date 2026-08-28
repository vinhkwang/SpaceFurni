-- placed orders and their line-item snapshots, immutable once written
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(20) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'PACKING', 'DELIVERED', 'CANCELLED')),
    subtotal_amount BIGINT NOT NULL,
    shipping_amount BIGINT NOT NULL,
    discount_amount BIGINT NOT NULL,
    total_amount BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',
    promotion_code VARCHAR(20) REFERENCES promotions(code),
    delivery_full_name VARCHAR(255) NOT NULL,
    delivery_phone VARCHAR(20) NOT NULL,
    delivery_street VARCHAR(255) NOT NULL,
    delivery_district VARCHAR(255) NOT NULL,
    delivery_city VARCHAR(255) NOT NULL,
    delivery_note VARCHAR(500),
    delivery_window VARCHAR(20) NOT NULL CHECK (delivery_window IN ('STANDARD', 'NEXT_DAY')),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CARD', 'CASH_ON_DELIVERY', 'BANK_TRANSFER')),
    payment_status VARCHAR(20) NOT NULL CHECK (payment_status IN ('PENDING', 'AUTHORISED', 'CAPTURED', 'FAILED')),
    placed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_orders_user_id_placed_at ON orders (user_id, placed_at DESC);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    product_name_snapshot VARCHAR(255) NOT NULL,
    sku_snapshot VARCHAR(64) NOT NULL,
    unit_price_amount BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    line_total_amount BIGINT NOT NULL
);
