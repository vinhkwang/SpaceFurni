-- product images, specifications and colour swatches, cascade-deleted with their product
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE product_specifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_key VARCHAR(100) NOT NULL,
    spec_value VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE product_color_swatches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    hex_code VARCHAR(7) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);
