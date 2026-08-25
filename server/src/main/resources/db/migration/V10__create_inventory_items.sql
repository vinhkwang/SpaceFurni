-- inventory tracked separately from products to isolate write contention from catalogue reads
CREATE TABLE inventory_items (
    product_id UUID PRIMARY KEY REFERENCES products(id),
    quantity_on_hand INTEGER NOT NULL CHECK (quantity_on_hand >= 0),
    quantity_reserved INTEGER NOT NULL CHECK (quantity_reserved >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
