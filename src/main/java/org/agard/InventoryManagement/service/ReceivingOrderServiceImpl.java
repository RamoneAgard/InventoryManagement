package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.domain.OrderItem;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.agard.InventoryManagement.mappers.ReceivingOrderFormMapper;
import org.agard.InventoryManagement.repositories.ReceivingOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReceivingOrderServiceImpl implements ReceivingOrderService {

    private final ReceivingOrderRepository orderRepository;

    private final OrderItemService itemService;

    private final ReceivingOrderFormMapper formMapper;

    private final Integer DEFAULT_PAGE_SIZE = 20;

    private final Integer MAX_PAGE_SIZE = 50;


    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){

        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > MAX_PAGE_SIZE)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        Sort defaultSort = Sort.by("createdDate").descending();

        return PageRequest.of(pageNumber, pageSize, defaultSort);
    }

    @Override
    public Page<ReceivingOrder> filterOrderPage(String supplier, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if(!StringUtils.hasText(supplier)){
            supplier = null;
        }

        return orderRepository.findAllWithFilters(supplier, createdBefore, pageRequest);
    }

    @Override
    public void saveOrder(ReceivingOrderForm orderForm) {
        ReceivingOrder orderToSave;
        if(orderForm.getId() == null){
            orderToSave = new ReceivingOrder();
        }
        else {
            orderToSave = getById(orderForm.getId());
        }

        Set<OrderItem> itemsToSave = new HashSet<>();
        for(OrderItemForm item : orderForm.getItems()){
            itemsToSave.add(
                    itemService.updateOrCreateOrderItem(item, false)
            );
        }
        orderToSave.setItems(itemsToSave);
        orderToSave.setSupplier(orderForm.getSupplier());
        orderRepository.save(orderToSave);
    }

    @Override
    public ReceivingOrderForm getFormById(Long id) {
        ReceivingOrder order = getById(id);
        return formMapper.receivingOrderToReceivingOrderForm(order);
    }

    @Override
    public ReceivingOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    throw new NotFoundException("Receiving Order not found for ID: " + id);
                });
    }

    @Override
    public void deleteById(Long id) {
        if(orderRepository.existsById(id)){
            orderRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("Receiving Order not found for ID: " + id);
    }
}
