package org.agard.InventoryManagement.mappers;

import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = ProductMapper.class)
public interface OrderItemFormMapper {

    OrderItemForm OrderItemToOrderItemForm(OrderItem orderItem);
}
