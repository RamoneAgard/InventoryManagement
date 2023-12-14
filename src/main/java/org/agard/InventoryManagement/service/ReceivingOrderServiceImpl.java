package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
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
public class ReceivingOrderServiceImpl implements ReceivingOrderService, PagingService {

    private final ReceivingOrderRepository orderRepository;

    private final OrderItemService itemService;

    private final ReceivingOrderFormMapper formMapper;

    private final Sort defaultSort = Sort.by("createdDate").descending();


    @Override
    public Page<ReceivingOrder> filterOrderPage(String supplier, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(supplier)){
            supplier = null;
        }

        return orderRepository.findAllWithFilters(supplier, createdBefore, pageRequest);
    }

    @Override
    @Transactional
    public void saveOrder(ReceivingOrderForm orderForm) {

        Set<OrderItem> itemsToSave = new HashSet<>();
        for(OrderItemForm item : orderForm.getItems()){
            itemsToSave.add(
                    itemService.updateOrCreateOrderItem(item, false)
            );
        }

        ReceivingOrder orderToSave;
        Set<OrderItem> itemsToDelete = new HashSet<>();
        if(orderForm.getId() == null){
            orderToSave = new ReceivingOrder();
        }
        else {
            orderToSave = getById(orderForm.getId());
            for(OrderItem oldItem : orderToSave.getItems()){
                if(!itemsToSave.contains(oldItem)){
                    itemsToDelete.add(oldItem);
                }
            }
        }

        orderToSave.setItems(itemsToSave);
        orderToSave.setSupplier(orderForm.getSupplier());
        try{
            orderRepository.save(orderToSave);
        }
        catch (RuntimeException e){
            throw new ItemCreationException("Something went wrong saving this order");
        }

        itemService.revertInventory(itemsToDelete, false);
        for(OrderItem item : itemsToDelete){
            itemService.deleteById(item.getId());
        }
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
            itemService.revertInventory(getById(id).getItems(), false);
            orderRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("Receiving Order not found for ID: " + id);
    }
}
