-- catalog categories, two levels: department (parent_id null) to subcategory
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_categories_parent_id ON categories (parent_id);
