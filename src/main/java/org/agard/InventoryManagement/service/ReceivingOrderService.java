package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ReceivingOrderService {

    Page<ReceivingOrder> filterOrderPage(String supplier, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize);

    void saveOrder(ReceivingOrderForm orderForm);

    ReceivingOrderForm getFormById(Long id);

    ReceivingOrder getById(Long id);

    void deleteById(Long id);
}
