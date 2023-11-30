package org.agard.InventoryManagement.mappers;

import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.mapstruct.Mapper;

@Mapper(uses = OrderItemFormMapper.class)
public interface OutgoingOrderFormMapper {

    OutgoingOrderForm outgoingOrderToOutgoingOrderForm(OutgoingOrder outgoingOrder);
}
