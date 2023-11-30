package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OutgoingOrderRepository extends JpaRepository<OutgoingOrder, Long> {

    Page<OutgoingOrder> findAllByReceiverContainingIgnoreCaseAndCreatedDateBefore(String receiver, LocalDateTime createdDate, Pageable pageable);

    @Query("SELECT o FROM OutgoingOrder o WHERE (:receiver is null or UPPER(o.receiver) LIKE UPPER(concat('%',:receiver, '%'))) and " +
            "(:createdBefore is null or o.createdDate < :createdBefore)")
    Page<OutgoingOrder> findAllWithFilters(@Param("receiver") String receiver, @Param("createdBefore") LocalDateTime createdBefore, Pageable pageable);
}
