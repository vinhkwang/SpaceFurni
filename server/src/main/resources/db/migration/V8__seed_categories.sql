-- five departments and their subcategories, per the mockup's DEPTS and NAV arrays
INSERT INTO categories (parent_id, name, slug, image_url, display_order) VALUES
    (NULL, 'Living room', 'living-room', '/images/room-living.jpg', 1),
    (NULL, 'Kitchen', 'kitchen', '/images/room-kitchen.jpg', 2),
    (NULL, 'Bedroom', 'bedroom', '/images/room-bedroom.png', 3),
    (NULL, 'Work & study', 'work-study', '/images/promo-office.jpg', 4),
    (NULL, 'Others', 'others', '/images/p-anita-shelf.jpg', 5);

INSERT INTO categories (parent_id, name, slug, image_url, display_order)
SELECT id, 'Sofa', 'sofa', NULL, 1 FROM categories WHERE slug = 'living-room'
UNION ALL
SELECT id, 'Coffee table', 'coffee-table', NULL, 2 FROM categories WHERE slug = 'living-room'
UNION ALL
SELECT id, 'Shelf', 'shelf', NULL, 3 FROM categories WHERE slug = 'living-room'
UNION ALL
SELECT id, 'Armchair', 'armchair', NULL, 4 FROM categories WHERE slug = 'living-room'
UNION ALL
SELECT id, 'Cupboard', 'cupboard', NULL, 1 FROM categories WHERE slug = 'kitchen'
UNION ALL
SELECT id, 'Sideboard', 'sideboard', NULL, 2 FROM categories WHERE slug = 'kitchen'
UNION ALL
SELECT id, 'Dining table', 'dining-table', NULL, 3 FROM categories WHERE slug = 'kitchen'
UNION ALL
SELECT id, 'Trolley', 'trolley', NULL, 4 FROM categories WHERE slug = 'kitchen'
UNION ALL
SELECT id, 'Bed', 'bed', NULL, 1 FROM categories WHERE slug = 'bedroom'
UNION ALL
SELECT id, 'Wardrobe', 'wardrobe', NULL, 2 FROM categories WHERE slug = 'bedroom'
UNION ALL
SELECT id, 'Bedside table', 'bedside-table', NULL, 3 FROM categories WHERE slug = 'bedroom'
UNION ALL
SELECT id, 'Chest of drawers', 'chest-of-drawers', NULL, 4 FROM categories WHERE slug = 'bedroom'
UNION ALL
SELECT id, 'Dressing table', 'dressing-table', NULL, 5 FROM categories WHERE slug = 'bedroom'
UNION ALL
SELECT id, 'Desk', 'desk', NULL, 1 FROM categories WHERE slug = 'work-study'
UNION ALL
SELECT id, 'Chair', 'chair', NULL, 2 FROM categories WHERE slug = 'work-study'
UNION ALL
SELECT id, 'Bookshelf', 'bookshelf', NULL, 3 FROM categories WHERE slug = 'work-study'
UNION ALL
SELECT id, 'Drinks cabinet', 'drinks-cabinet', NULL, 4 FROM categories WHERE slug = 'work-study'
UNION ALL
SELECT id, 'Lighting', 'lighting', NULL, 1 FROM categories WHERE slug = 'others'
UNION ALL
SELECT id, 'Rugs', 'rugs', NULL, 2 FROM categories WHERE slug = 'others';
