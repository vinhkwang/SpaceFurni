-- eight products from the mockup's SEED array, with images, specifications and colour swatches
INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-CLOUD-SOFA', 'Cloud 3-Seater Sofa', 'cloud-3-seater-sofa', id, 24900000, NULL, 'PUBLISHED',
    'A deep, low sofa with feather-topped foam cushions and a linen cover you can unzip and wash.',
    'A deep, low sofa with feather-topped foam cushions and a linen cover you can unzip and wash. Sit in it once and the rest of the range starts to feel formal.',
    'W 268 × D 96 × H 72 cm', 'Kiln-dried acacia frame, linen-cotton blend', 'Light grey linen', 4.8, 96, true, true
FROM categories WHERE slug = 'sofa' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-cloud-sofa.jpg', 1 FROM products WHERE slug = 'cloud-3-seater-sofa';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 268 × D 96 × H 72 cm', 1 FROM products WHERE slug = 'cloud-3-seater-sofa'
UNION ALL
SELECT id, 'Material', 'Kiln-dried acacia frame, linen-cotton blend', 2 FROM products WHERE slug = 'cloud-3-seater-sofa'
UNION ALL
SELECT id, 'Colour', 'Light grey linen', 3 FROM products WHERE slug = 'cloud-3-seater-sofa';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#C9C6C0', 1 FROM products WHERE slug = 'cloud-3-seater-sofa'
UNION ALL
SELECT id, '#8C8F8B', 2 FROM products WHERE slug = 'cloud-3-seater-sofa'
UNION ALL
SELECT id, '#3B3A38', 3 FROM products WHERE slug = 'cloud-3-seater-sofa';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-CLAIRE-SOFA', 'Claire 3-Seater Sofa', 'claire-3-seater-sofa', id, 19500000, 24400000, 'PUBLISHED',
    'Mid-century lines, buttoned back, tapered oak legs.',
    'Mid-century lines, buttoned back, tapered oak legs. Narrow enough for an apartment living room but still seats three properly.',
    'W 208 × D 88 × H 82 cm', 'Solid oak legs, brushed velvet', 'Powder blue', 4.6, 74, false, true
FROM categories WHERE slug = 'sofa' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-claire-sofa.jpg', 1 FROM products WHERE slug = 'claire-3-seater-sofa';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 208 × D 88 × H 82 cm', 1 FROM products WHERE slug = 'claire-3-seater-sofa'
UNION ALL
SELECT id, 'Material', 'Solid oak legs, brushed velvet', 2 FROM products WHERE slug = 'claire-3-seater-sofa'
UNION ALL
SELECT id, 'Colour', 'Powder blue', 3 FROM products WHERE slug = 'claire-3-seater-sofa';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#B8CEDC', 1 FROM products WHERE slug = 'claire-3-seater-sofa'
UNION ALL
SELECT id, '#D8D3C8', 2 FROM products WHERE slug = 'claire-3-seater-sofa'
UNION ALL
SELECT id, '#6F7C83', 3 FROM products WHERE slug = 'claire-3-seater-sofa';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-AXIS-TABLE', 'Axis Round Coffee Table', 'axis-round-coffee-table', id, 6400000, NULL, 'PUBLISHED',
    'A dark walnut top on a slim brushed-brass ring.',
    'A dark walnut top on a slim brushed-brass ring. Light enough to move with one hand when the room turns into a dance floor.',
    'Ø 80 × H 42 cm', 'Walnut veneer, powder-coated steel', 'Walnut & brass', 4.9, 58, false, true
FROM categories WHERE slug = 'coffee-table' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-axis-table.jpg', 1 FROM products WHERE slug = 'axis-round-coffee-table';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'Ø 80 × H 42 cm', 1 FROM products WHERE slug = 'axis-round-coffee-table'
UNION ALL
SELECT id, 'Material', 'Walnut veneer, powder-coated steel', 2 FROM products WHERE slug = 'axis-round-coffee-table'
UNION ALL
SELECT id, 'Colour', 'Walnut & brass', 3 FROM products WHERE slug = 'axis-round-coffee-table';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#6B4A31', 1 FROM products WHERE slug = 'axis-round-coffee-table'
UNION ALL
SELECT id, '#C99C64', 2 FROM products WHERE slug = 'axis-round-coffee-table'
UNION ALL
SELECT id, '#2B2A28', 3 FROM products WHERE slug = 'axis-round-coffee-table';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-ANITA-SHELF', 'Anita Wall Shelf', 'anita-wall-shelf', id, 3850000, NULL, 'PUBLISHED',
    'Three oak planks held by dowel pegs — no visible brackets, no drilling into the shelf itself.',
    'Three oak planks held by dowel pegs — no visible brackets, no drilling into the shelf itself. Add or remove a level whenever the books multiply.',
    'W 140 × D 24 × H 74 cm', 'Solid white oak, hardwax oil', 'Natural oak', 4.7, 41, false, false
FROM categories WHERE slug = 'shelf' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-anita-shelf.jpg', 1 FROM products WHERE slug = 'anita-wall-shelf';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 140 × D 24 × H 74 cm', 1 FROM products WHERE slug = 'anita-wall-shelf'
UNION ALL
SELECT id, 'Material', 'Solid white oak, hardwax oil', 2 FROM products WHERE slug = 'anita-wall-shelf'
UNION ALL
SELECT id, 'Colour', 'Natural oak', 3 FROM products WHERE slug = 'anita-wall-shelf';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#D8B98C', 1 FROM products WHERE slug = 'anita-wall-shelf'
UNION ALL
SELECT id, '#A9825A', 2 FROM products WHERE slug = 'anita-wall-shelf'
UNION ALL
SELECT id, '#EFE7DA', 3 FROM products WHERE slug = 'anita-wall-shelf';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-HALDEN-CHAIR', 'Halden Tub Chair', 'halden-tub-chair', id, 7200000, NULL, 'PUBLISHED',
    'A small chair that wraps around you — the one people fight over at parties.',
    'A small chair that wraps around you — the one people fight over at parties. Fits in the corner a full armchair can''t.',
    'W 72 × D 70 × H 74 cm', 'Beech frame, woven poly-cotton', 'Pebble grey', 4.5, 33, true, false
FROM categories WHERE slug = 'armchair' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-tub-chair.png', 1 FROM products WHERE slug = 'halden-tub-chair';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 72 × D 70 × H 74 cm', 1 FROM products WHERE slug = 'halden-tub-chair'
UNION ALL
SELECT id, 'Material', 'Beech frame, woven poly-cotton', 2 FROM products WHERE slug = 'halden-tub-chair'
UNION ALL
SELECT id, 'Colour', 'Pebble grey', 3 FROM products WHERE slug = 'halden-tub-chair';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#CFCCC7', 1 FROM products WHERE slug = 'halden-tub-chair'
UNION ALL
SELECT id, '#8B857C', 2 FROM products WHERE slug = 'halden-tub-chair'
UNION ALL
SELECT id, '#3E3B37', 3 FROM products WHERE slug = 'halden-tub-chair';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-ROWAN-TROLLEY', 'Rowan Kitchen Trolley', 'rowan-kitchen-trolley', id, 5100000, 6300000, 'PUBLISHED',
    'Stainless top, two cupboards, a towel bar and locking castors.',
    'Stainless top, two cupboards, a towel bar and locking castors. Becomes prep space, bar cart, or the place the rice cooker finally lives.',
    'W 66 × D 45 × H 88 cm', 'Rubberwood, stainless steel top', 'Honey oak', 4.4, 27, false, false
FROM categories WHERE slug = 'trolley' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-trolley.png', 1 FROM products WHERE slug = 'rowan-kitchen-trolley';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 66 × D 45 × H 88 cm', 1 FROM products WHERE slug = 'rowan-kitchen-trolley'
UNION ALL
SELECT id, 'Material', 'Rubberwood, stainless steel top', 2 FROM products WHERE slug = 'rowan-kitchen-trolley'
UNION ALL
SELECT id, 'Colour', 'Honey oak', 3 FROM products WHERE slug = 'rowan-kitchen-trolley';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#C4884A', 1 FROM products WHERE slug = 'rowan-kitchen-trolley'
UNION ALL
SELECT id, '#E4D6C1', 2 FROM products WHERE slug = 'rowan-kitchen-trolley'
UNION ALL
SELECT id, '#4A3A2A', 3 FROM products WHERE slug = 'rowan-kitchen-trolley';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-SPINDLE-BEDSIDE', 'Spindle Bedside Table', 'spindle-bedside-table', id, 4300000, NULL, 'PUBLISHED',
    'One soft-close drawer, one open shelf, and a top wide enough for a lamp, a book and a glass of water at the same time.',
    'One soft-close drawer, one open shelf, and a top wide enough for a lamp, a book and a glass of water at the same time.',
    'W 52 × D 40 × H 55 cm', 'Solid oak, linoleum top', 'Natural oak', 4.8, 62, true, true
FROM categories WHERE slug = 'bedside-table' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-bedside.png', 1 FROM products WHERE slug = 'spindle-bedside-table';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 52 × D 40 × H 55 cm', 1 FROM products WHERE slug = 'spindle-bedside-table'
UNION ALL
SELECT id, 'Material', 'Solid oak, linoleum top', 2 FROM products WHERE slug = 'spindle-bedside-table'
UNION ALL
SELECT id, 'Colour', 'Natural oak', 3 FROM products WHERE slug = 'spindle-bedside-table';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#D8B98C', 1 FROM products WHERE slug = 'spindle-bedside-table'
UNION ALL
SELECT id, '#EFE7DA', 2 FROM products WHERE slug = 'spindle-bedside-table'
UNION ALL
SELECT id, '#3E3B37', 3 FROM products WHERE slug = 'spindle-bedside-table';

INSERT INTO products (sku, name, slug, category_id, price_amount, compare_at_price_amount, status,
    short_description, long_description, dimensions, material, primary_color_name, rating_average, review_count,
    is_new, is_bestseller)
SELECT 'SF-MERIDIAN-DESK', 'Meridian Writing Desk', 'meridian-writing-desk', id, 11700000, NULL, 'PUBLISHED',
    'A calm walnut desk with a three-drawer pedestal that can sit left or right, and a cable channel that hides the mess at the back.',
    'A calm walnut desk with a three-drawer pedestal that can sit left or right, and a cable channel that hides the mess at the back.',
    'W 160 × D 75 × H 74 cm', 'Walnut veneer, solid walnut legs', 'Walnut', 4.7, 45, false, false
FROM categories WHERE slug = 'desk' AND parent_id IS NOT NULL;

INSERT INTO product_images (product_id, url, display_order)
SELECT id, '/images/p-desk.png', 1 FROM products WHERE slug = 'meridian-writing-desk';
INSERT INTO product_specifications (product_id, spec_key, spec_value, display_order)
SELECT id, 'Dimensions', 'W 160 × D 75 × H 74 cm', 1 FROM products WHERE slug = 'meridian-writing-desk'
UNION ALL
SELECT id, 'Material', 'Walnut veneer, solid walnut legs', 2 FROM products WHERE slug = 'meridian-writing-desk'
UNION ALL
SELECT id, 'Colour', 'Walnut', 3 FROM products WHERE slug = 'meridian-writing-desk';
INSERT INTO product_color_swatches (product_id, hex_code, display_order)
SELECT id, '#7A5636', 1 FROM products WHERE slug = 'meridian-writing-desk'
UNION ALL
SELECT id, '#2B2A28', 2 FROM products WHERE slug = 'meridian-writing-desk'
UNION ALL
SELECT id, '#D8B98C', 3 FROM products WHERE slug = 'meridian-writing-desk';
