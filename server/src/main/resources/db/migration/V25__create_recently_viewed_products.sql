-- recently viewed products belong to either a signed-in user or a guest token, never both
CREATE TABLE recently_viewed_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    guest_token UUID,
    product_id UUID NOT NULL REFERENCES products(id),
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK ((user_id IS NOT NULL AND guest_token IS NULL) OR (user_id IS NULL AND guest_token IS NOT NULL)),
    UNIQUE (user_id, product_id),
    UNIQUE (guest_token, product_id)
);
