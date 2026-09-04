-- records which colour swatch was selected when a line was added to the cart
ALTER TABLE cart_items ADD COLUMN color_hex_code VARCHAR(7);
