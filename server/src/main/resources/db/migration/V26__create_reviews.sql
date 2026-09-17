-- one review per purchased order line, enforced by the unique constraint below
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    user_id UUID NOT NULL REFERENCES users(id),
    order_item_id UUID NOT NULL UNIQUE REFERENCES order_items(id),
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PUBLISHED', 'HIDDEN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reviews_product_id_status ON reviews (product_id, status);
