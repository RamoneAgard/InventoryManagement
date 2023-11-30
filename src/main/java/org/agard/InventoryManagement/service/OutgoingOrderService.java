package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface OutgoingOrderService {

    Page<OutgoingOrder> filterOrderPage(String receiver, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize);

    void saveOrder(OutgoingOrderForm orderForm);

    OutgoingOrderForm getFormById(Long id);

    OutgoingOrder getById(Long id);

    void deleteById(Long id);
}
