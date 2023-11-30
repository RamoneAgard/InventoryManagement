package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;

public interface OrderItemService {

    OrderItem getById(Long id);

    OrderItem updateOrCreateOrderItem(OrderItemForm orderItemForm, boolean outgoing);

    OrderItemForm createItemFormByItemCode(String itemCode);

}
