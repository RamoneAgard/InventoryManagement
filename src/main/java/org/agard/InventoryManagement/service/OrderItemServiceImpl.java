package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.domain.OrderItem;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.OrderItemRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService{

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
    @Transactional
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
                        " exceeds the current item stock. If this is correct, manually adjust the item stock" +
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

        try{
            return itemRepository.save(itemToSave);
        }
        catch (RuntimeException e){
            throw new ItemCreationException("Something went wrong saving this order line");
        }

    }

    @Override
    public OrderItemForm createItemFormByItemCode(String itemCode) {
        OrderItemForm itemForm = new OrderItemForm();
        itemForm.setProduct(
                productService.getItemProductByCode(itemCode)
        );
        return itemForm;
    }

    @Override
    @Transactional
    public void revertInventory(Collection<OrderItem> items, boolean outgoing) {
        List<Product> productsToRevert = new ArrayList<>();

        for(OrderItem item : items){
            Product itemProduct = item.getProduct();
            if(outgoing){
                itemProduct.setStock(
                        itemProduct.getStock() + item.getQuantity()
                );
            }
            else{
                if(itemProduct.getStock() < item.getQuantity()){
                    throw new StockException("Reverting the received inventory on this order for " +
                            "item: " + itemProduct.getItemCode() + " exceeds the currently " +
                            "available stock. To continue, manually adjust the item stock before" +
                            "deleting this order.");
                }
                itemProduct.setStock(
                        itemProduct.getStock() - item.getQuantity()
                );
            }
            productsToRevert.add(itemProduct);
        }

        for(Product revertProduct : productsToRevert){
            productService.saveProduct(revertProduct);
        }
    }

    @Override
    public void deleteById(Long id) {
        if(itemRepository.existsById(id)){
            itemRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("Order line not found for ID: " + id);
    }
}
