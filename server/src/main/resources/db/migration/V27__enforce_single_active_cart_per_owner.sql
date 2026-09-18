-- collapse duplicate active carts created by a resolve-or-create race before enforcing uniqueness
WITH ranked_active_user_carts AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY updated_at DESC, id DESC) AS rank
    FROM carts
    WHERE status = 'ACTIVE' AND user_id IS NOT NULL
)
UPDATE carts
SET status = 'ABANDONED'
WHERE id IN (SELECT id FROM ranked_active_user_carts WHERE rank > 1);

WITH ranked_active_guest_carts AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY guest_token ORDER BY updated_at DESC, id DESC) AS rank
    FROM carts
    WHERE status = 'ACTIVE' AND guest_token IS NOT NULL
)
UPDATE carts
SET status = 'ABANDONED'
WHERE id IN (SELECT id FROM ranked_active_guest_carts WHERE rank > 1);

CREATE UNIQUE INDEX ux_carts_active_user ON carts (user_id) WHERE status = 'ACTIVE' AND user_id IS NOT NULL;
CREATE UNIQUE INDEX ux_carts_active_guest_token ON carts (guest_token) WHERE status = 'ACTIVE' AND guest_token IS NOT NULL;
