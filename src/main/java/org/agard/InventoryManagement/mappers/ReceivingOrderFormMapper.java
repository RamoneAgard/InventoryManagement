package org.agard.InventoryManagement.mappers;

import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {OrderItemFormMapper.class})
public interface ReceivingOrderFormMapper {

    ReceivingOrderForm receivingOrderToReceivingOrderForm(ReceivingOrder receivingOrder);
}
