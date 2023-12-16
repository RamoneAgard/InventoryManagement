package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface ReceivingOrderService {

    /**
     * @param supplier filter value to compare with the 'supplier' field of ReceivingOrder objects for similarity
     * @param createdBefore filter value to compare with the 'createdDate' field of ReceivingOrder objects representing cutoff date and time
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of ReceivingOrder objects filtered by the supplied params
     */
    Page<ReceivingOrder> filterOrderPage(String supplier, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize);


    /**
     * Creates/Updates and persists a ReceivingOrder object to the datasource using date from
     * the 'orderForm' param.
     *
     * @param orderForm form with corresponding data to create of update ReceivingOrder object in datasource
     * @throws NotFoundException if orderFrom contains a non-null 'id' field and corresponding ReceivingOrder object is not found
     */
    void saveOrder(ReceivingOrderForm orderForm);


    /**
     * @param id 'id' field value of the Receiving object to retrieve from the datasource
     * @return ReceivingOrderForm object mapped from the retrieved ReceivingOrder object
     * @throws NotFoundException if item is not found in the datasource
     */
    ReceivingOrderForm getFormById(Long id);


    /**
     * @param id 'id' field value of the ReceivingOrder object to retrieve from the datasource
     * @return ReceivingOrder object
     * @throws NotFoundException if item is not found in datasource
     */
    ReceivingOrder getById(Long id);


    /**
     * @param id 'id' field value of the ReceivingOrder object to delete from the datasource
     * @throws NotFoundException if item is not found in datasource
     */
    void deleteById(Long id);
}
