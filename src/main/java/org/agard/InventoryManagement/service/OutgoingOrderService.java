package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface OutgoingOrderService {

    /**
     * @param receiver filter value to compare with the 'receiver' field of OutgoingOrder objects for similarity
     * @param createdBefore filter value to compare with the 'createdDate' field of OutgoingOrder objects representing cutoff date and time
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of OutgoingOrder objects filtered by the supplied params
     */
    Page<OutgoingOrder> filterOrderPage(String receiver, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize);


    /**
     * Creates/Updates and persists an OutgoingOrder object to the datasource using date from
     * the 'orderForm' param.
     *
     * @param orderForm form with corresponding data to create or update OutgoingOrder object in datasource
     * @throws NotFoundException if orderFrom contains a non-null 'id' field and corresponding OutgoingOrder object is not found
     */
    void saveOrder(OutgoingOrderForm orderForm);


    /**
     * @param id 'id' field value of the OutgoingOrder object to retrieve from the datasource
     * @return OutgoingOrderForm object mapped from the retrieved OutgoingOrder object
     * @throws NotFoundException if item is not found in the datasource
     */
    OutgoingOrderForm getFormById(Long id);


    /**
     * @param id 'id' field value of the OutgoingOrder object to retrieve from the datasource
     * @return OutgoingOrder object
     * @throws NotFoundException if item is not found in datasource
     */
    OutgoingOrder getById(Long id);


    /**
     * @param id 'id' field value of the OutgoingOrder object to delete from the datasource
     * @throws NotFoundException if item is not found in datasource
     */
    void deleteById(Long id);
}
