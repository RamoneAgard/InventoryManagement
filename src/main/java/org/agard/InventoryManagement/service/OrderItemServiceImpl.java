package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.OrderItemRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository itemRepository;

    private final ProductService productService;

    @Override
    public OrderItem getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    throw new NotFoundException("Order line not found for ID: " + id +
                            ". Please reload and try again");
                });
    }

    @Override
    public OrderItem updateOrCreateOrderItem(OrderItemForm orderItemForm, boolean outgoing) {
        OrderItem itemToSave;
        boolean isOld = false;
        if(orderItemForm.getId() == null){
            itemToSave = new OrderItem();
        }
        else {
            itemToSave = getById(orderItemForm.getId());
            isOld = true;
        }

        Product itemProduct = productService.getById(orderItemForm.getProduct().getId());
        if(outgoing){
            int stockUpdate = itemProduct.getStock();
            if(isOld){
                stockUpdate -= (orderItemForm.getQuantity() - itemToSave.getQuantity());
            }
            else{
                stockUpdate -= orderItemForm.getQuantity();
            }
            if(stockUpdate < 0){
                throw new StockException("The input quantity for item: " + itemProduct.getItemCode() +
                        " exceeds the current item stock. If this is correct, manually adjust the product stock" +
                        "before creating this order.");
            }
            itemProduct.setStock(stockUpdate);
            itemProduct.setPrice(orderItemForm.getPrice());
        }
        else {
            int stockUpdate = itemProduct.getStock();
            if(isOld){
                stockUpdate += (orderItemForm.getQuantity() - itemToSave.getQuantity());
            }
            else {
                stockUpdate += orderItemForm.getQuantity();
            }
            itemProduct.setStock(stockUpdate);
            itemProduct.setCost(orderItemForm.getPrice());
        }
        itemProduct = productService.saveProduct(itemProduct);

        itemToSave.setProduct(itemProduct);
        itemToSave.setQuantity(orderItemForm.getQuantity());
        itemToSave.setPrice(orderItemForm.getPrice());

        return itemRepository.save(itemToSave);
    }

    @Override
    public OrderItemForm createItemFormByItemCode(String itemCode) {
        OrderItemForm itemForm = new OrderItemForm();
        itemForm.setProduct(
                productService.getItemProductByCode(itemCode)
        );
        return itemForm;
    }
}
