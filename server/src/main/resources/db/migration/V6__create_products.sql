-- products catalogue, price optimistic-locked with version
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    category_id UUID NOT NULL REFERENCES categories(id),
    price_amount BIGINT NOT NULL,
    compare_at_price_amount BIGINT,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    short_description VARCHAR(500),
    long_description TEXT,
    dimensions VARCHAR(255),
    material VARCHAR(255),
    primary_color_name VARCHAR(100),
    rating_average NUMERIC(2,1),
    review_count INTEGER NOT NULL DEFAULT 0,
    is_new BOOLEAN NOT NULL DEFAULT false,
    is_bestseller BOOLEAN NOT NULL DEFAULT false,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_slug ON products (slug);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_price_amount ON products (price_amount);
