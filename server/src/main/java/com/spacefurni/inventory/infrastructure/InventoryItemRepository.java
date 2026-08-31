package com.spacefurni.inventory.infrastructure;

import com.spacefurni.inventory.domain.InventoryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE InventoryItem i
               SET i.quantityOnHand = i.quantityOnHand - :quantity,
                   i.quantityReserved = i.quantityReserved + :quantity,
                   i.updatedAt = CURRENT_TIMESTAMP
             WHERE i.productId = :productId
               AND i.quantityOnHand >= :quantity
            """)
    int decrementQuantityOnHandIfSufficient(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE InventoryItem i
               SET i.quantityOnHand = i.quantityOnHand + :quantity,
                   i.quantityReserved = i.quantityReserved - :quantity,
                   i.updatedAt = CURRENT_TIMESTAMP
             WHERE i.productId = :productId
            """)
    int incrementQuantityOnHand(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE InventoryItem i
               SET i.quantityOnHand = i.quantityOnHand + :delta,
                   i.updatedAt = CURRENT_TIMESTAMP
             WHERE i.productId = :productId
               AND i.quantityOnHand + :delta >= 0
            """)
    int adjustQuantityOnHandIfSufficient(@Param("productId") UUID productId, @Param("delta") int delta);

    List<InventoryItem> findAllByProductIdInOrderByProductIdAsc(List<UUID> productIds);

    long countByQuantityOnHandLessThan(int threshold);
}
