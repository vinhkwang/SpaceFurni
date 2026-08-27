-- one inventory row per seeded product, stock figures from the mockup's SEED array
INSERT INTO inventory_items (product_id, quantity_on_hand, quantity_reserved)
SELECT id, 6, 0 FROM products WHERE slug = 'cloud-3-seater-sofa'
UNION ALL
SELECT id, 4, 0 FROM products WHERE slug = 'claire-3-seater-sofa'
UNION ALL
SELECT id, 14, 0 FROM products WHERE slug = 'axis-round-coffee-table'
UNION ALL
SELECT id, 22, 0 FROM products WHERE slug = 'anita-wall-shelf'
UNION ALL
SELECT id, 9, 0 FROM products WHERE slug = 'halden-tub-chair'
UNION ALL
SELECT id, 11, 0 FROM products WHERE slug = 'rowan-kitchen-trolley'
UNION ALL
SELECT id, 18, 0 FROM products WHERE slug = 'spindle-bedside-table'
UNION ALL
SELECT id, 7, 0 FROM products WHERE slug = 'meridian-writing-desk';
