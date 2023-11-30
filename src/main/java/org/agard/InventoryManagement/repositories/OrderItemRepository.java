package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
