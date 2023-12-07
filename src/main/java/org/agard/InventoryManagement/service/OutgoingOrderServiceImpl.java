package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.domain.OrderItem;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.mappers.OutgoingOrderFormMapper;
import org.agard.InventoryManagement.repositories.OrderItemRepository;
import org.agard.InventoryManagement.repositories.OutgoingOrderRepository;
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
public class OutgoingOrderServiceImpl implements OutgoingOrderService {

    private final OutgoingOrderRepository orderRepository;

    private final OrderItemService itemService;

    private final OutgoingOrderFormMapper formMapper;

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
    public Page<OutgoingOrder> filterOrderPage(String receiver, LocalDateTime createdBefore, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if(!StringUtils.hasText(receiver)){
            receiver = null;
        }

        return orderRepository.findAllWithFilters(receiver, createdBefore, pageRequest);
    }

    @Override
    public void saveOrder(OutgoingOrderForm orderForm) {

        Set<OrderItem> itemsToSave = new HashSet<>();
        for(OrderItemForm item : orderForm.getItems()){
            itemsToSave.add(
                itemService.updateOrCreateOrderItem(item, true)
            );
        }

        OutgoingOrder orderToSave;
        Set<OrderItem> itemsToDelete = new HashSet<>();
        if(orderForm.getId() == null){
            orderToSave = new OutgoingOrder();
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
        orderToSave.setReceiver(orderForm.getReceiver());
        orderRepository.save(orderToSave);

        itemService.revertInventory(itemsToDelete, true);
        for(OrderItem item : itemsToDelete){
            itemService.deleteById(item.getId());
        }
    }


    @Override
    public OutgoingOrderForm getFormById(Long id) {
        OutgoingOrder order = getById(id);
        return formMapper.outgoingOrderToOutgoingOrderForm(order);
    }


    @Override
    public OutgoingOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    throw new NotFoundException("Outgoing Order not found for ID: " + id);
                });
    }

    @Override
    public void deleteById(Long id) {
        if(orderRepository.existsById(id)){
            itemService.revertInventory(getById(id).getItems(), true);
            orderRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("Outgoing Order not found for ID: " + id);
    }
}
