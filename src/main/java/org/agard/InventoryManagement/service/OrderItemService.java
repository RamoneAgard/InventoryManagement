package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;

import java.util.Collection;

public interface OrderItemService {

    /**
     * @param id 'id' field value of OrderItem object to retrieve from the datasource
     * @return OrderItem object
     * @throws NotFoundException if item is not found in datasource
     */
    OrderItem getById(Long id);


    /**
     * Creates and persists an OrderItem object to the datasource using the data from the 'orderItemForm' param.
     *
     * @param orderItemForm form with corresponding data to create or update OrderItem object in datasource
     * @param outgoing boolean value to indicate if this OrderItem corresponds to an OutgoingOrder object (false defaults to ReceivingOrder object)
     * @return the OrderItem object after it has been saved to the dataSource
     * @throws StockException if updating 'stock' field values results in a negative value
     */
    OrderItem updateOrCreateOrderItem(OrderItemForm orderItemForm, boolean outgoing);


    /**
     * @param itemCode 'itemCode' field value that corresponds to a Product object whose data is added to the return value
     * @return a OrderItemForm with Product object data retrieved using the itemCode param, other fields of the form are not populated
     * @throws NotFoundException if Product object cannot be found with the given itemCode value
     */
    OrderItemForm createItemFormByItemCode(String itemCode);


    /**
     * @param items List of OrderItem objects
     * @param outgoing boolean value to indicate if this OrderItem corresponds to an OutgoingOrder object (false defaults to ReceivingOrder object)
     * @throws StockException if reverting 'stock' field values violates datasource constraints
     */
    void revertInventory(Collection<OrderItem> items, boolean outgoing);


    /**
     * @param id 'id' field value of OrderItem object to delete from the datasource
     * @throws NotFoundException if object is not found in the datasource
     */
    void deleteById(Long id);

}
