package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReceivingOrderRepository extends JpaRepository<ReceivingOrder, Long> {

    @Query("SELECT o FROM ReceivingOrder o WHERE (:supplier is null or UPPER(o.supplier) LIKE UPPER(concat('%',:supplier, '%'))) and " +
            "(:createdBefore is null or o.createdDate < :createdBefore)")
    Page<ReceivingOrder> findAllWithFilters(@Param("supplier") String supplier, @Param("createdBefore") LocalDateTime createdBefore, Pageable pageable);
}
