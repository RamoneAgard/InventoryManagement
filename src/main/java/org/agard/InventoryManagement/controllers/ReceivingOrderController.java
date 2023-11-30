package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.domain.ReceivingOrder;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.OrderItemService;
import org.agard.InventoryManagement.service.ReceivingOrderService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class ReceivingOrderController {

    public static final String ORDER_PAGE_PATH = "/inorders";

    public static final String ORDER_UPDATE_PATH = "/inorders/update";

    public static final String ORDER_ADD_FORM_ITEM = "/inorders/update/add";

    public static final String ORDER_REMOVE_FORM_ITEM = "/inorders/update/remove";

    public static final String ORDER_TABLE_PATH = "/inorders/table";

    public static final String ORDER_DELETE_PATH = "/inorders/delete";

    private final ReceivingOrderService orderService;

    private final OrderItemService itemService;

    private final CategoryService categoryService;

    private final VolumeService volumeService;

    @GetMapping(ORDER_PAGE_PATH)
    public String getOutOrdersPage(Model model){

        model.addAttribute("receivingOrderForm", new ReceivingOrderForm());
        model.addAttribute("orders", orderService.filterOrderPage(null, null, null, null));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("volumes", volumeService.getAllVolumes());

        return ViewNames.RECEIVING_ORDER_VIEW;
    }

    @GetMapping(ORDER_UPDATE_PATH)
    public String getUpdateForm(@RequestParam(required = false) Long id,
                                Model model){
        ReceivingOrderForm orderForm;
        if(id == null){
            orderForm = new ReceivingOrderForm();
        }
        else {
            orderForm = orderService.getFormById(id);
        }
        model.addAttribute("receivingOrderForm", orderForm);

        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_ADD_FORM_ITEM)
    public String addItemToForm(ReceivingOrderForm orderForm,
                                @RequestParam(name = "code") String itemCode,
                                Model model){
        try{
            List<OrderItemForm> items = orderForm.getItems();
            OrderItemForm itemForm = itemService.createItemFormByItemCode(itemCode);
            if(!items.contains(itemForm)){
                items.add(itemForm);
            }
            else {
                model.addAttribute("addError", "Cannot add duplicate products to order");
            }
        } catch (NotFoundException e){
            model.addAttribute("addError", e.getMessage());
        }

        System.out.println(orderForm);
        model.addAttribute("receivingOrderForm", orderForm);

        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_REMOVE_FORM_ITEM)
    public String removeItemFromForm(ReceivingOrderForm orderForm,
                                     @RequestParam(name = "index") Integer index,
                                     Model model){

        orderForm.getItems().remove(index.intValue());
        model.addAttribute("receivingOrderFrom", orderForm);

        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid ReceivingOrderForm orderForm,
                                        BindingResult bindingResult,
                                        Model model){
        System.out.println(orderForm);
        if(orderForm.getItems().size() < 1){
            model.addAttribute("addError", "Cannot create order with zero items!");
        }
        else if(!bindingResult.hasErrors()){
            try{
                orderService.saveOrder(orderForm);
                model.addAttribute("receivingOrderForm", new ReceivingOrderForm());
            }
            catch (StockException e){
                model.addAttribute("addError", e.getMessage());
            }
        }
        else{
            if(bindingResult.hasFieldErrors("supplier")){
                model.addAttribute("addError",
                        bindingResult.getFieldError("supplier").toString()
                );
            }
        }
        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @RequestMapping(value = ORDER_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getOrdersTable(@RequestParam(required = false, name = "contact") String supplierQuery,
                                 @RequestParam(required = false, name = "createdBefore") LocalDateTime createdBeforeQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model){
        addPageToModel(supplierQuery, createdBeforeQuery, pageNumber, pageSize, model);

        return ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT;
    }

    @GetMapping(ORDER_DELETE_PATH)
    public String deleteOrderById(@RequestParam Long id,
                                  @RequestParam(required = false, name = "contact") String supplierQuery,
                                  @RequestParam(required = false, name = "createdBefore")LocalDateTime createdBeforeQuery,
                                  @RequestParam(defaultValue = "0") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  Model model){

        orderService.deleteById(id);
        addPageToModel(supplierQuery, createdBeforeQuery, pageNumber, pageSize, model);

        return ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT;
    }

    private void addPageToModel(String supplier,
                                LocalDateTime createdBefore,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){
        Page<ReceivingOrder> ordersPage = orderService.filterOrderPage(supplier, createdBefore, pageNumber, pageSize);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("contactQuery", supplier);
        model.addAttribute("createdBeforeQuery", createdBefore);

    }

}
