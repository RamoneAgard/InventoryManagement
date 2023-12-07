package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;

import java.util.Collection;

public interface OrderItemService {

    OrderItem getById(Long id);

    OrderItem updateOrCreateOrderItem(OrderItemForm orderItemForm, boolean outgoing);

    OrderItemForm createItemFormByItemCode(String itemCode);

    void revertInventory(Collection<OrderItem> items, boolean outgoing);

    void deleteById(Long id);

}
