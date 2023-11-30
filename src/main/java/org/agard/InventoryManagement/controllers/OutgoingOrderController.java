package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.domain.OutgoingOrder;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.OrderItemService;
import org.agard.InventoryManagement.service.OutgoingOrderService;
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
public class OutgoingOrderController {

    public static final String ORDER_PAGE_PATH = "/outorders";

    public static final String ORDER_UPDATE_PATH = "/outorders/update";

    public static final String ORDER_ADD_FORM_ITEM = "/outorders/update/add";

    public static final String ORDER_REMOVE_FORM_ITEM = "/outorders/update/remove";

    public static final String ORDER_TABLE_PATH = "/outorders/table";

    public static final String ORDER_DELETE_PATH = "/outorders/delete";

    private final OutgoingOrderService orderService;

    private final OrderItemService itemService;

    private final CategoryService categoryService;

    private final VolumeService volumeService;


    @GetMapping(ORDER_PAGE_PATH)
    public String getOutOrdersPage(Model model){

        model.addAttribute("outgoingOrderForm", new OutgoingOrderForm());
        model.addAttribute("orders", orderService.filterOrderPage(null, null, null, null));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("volumes", volumeService.getAllVolumes());

        return ViewNames.OUTGOING_ORDER_VIEW;
    }

    @GetMapping(ORDER_UPDATE_PATH)
    public String getUpdateForm(@RequestParam(required = false) Long id,
                                Model model){
        OutgoingOrderForm orderForm;
        if(id == null){
            orderForm = new OutgoingOrderForm();
        }
        else {
            orderForm = orderService.getFormById(id);
        }
        model.addAttribute("outgoingOrderForm", orderForm);

        return ViewNames.OUTGOING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_ADD_FORM_ITEM)
    public String addItemToForm(OutgoingOrderForm orderForm,
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
        model.addAttribute("outgoingOrderForm", orderForm);

        return ViewNames.OUTGOING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_REMOVE_FORM_ITEM)
    public String removeItemFromForm(OutgoingOrderForm orderForm,
                                     @RequestParam(name = "index") Integer index,
                                     Model model){

        orderForm.getItems().remove(index.intValue());
        model.addAttribute("outgoingOrderFrom", orderForm);

        return ViewNames.OUTGOING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid OutgoingOrderForm orderForm,
                                        BindingResult bindingResult,
                                        Model model){
        System.out.println(orderForm);
        if(orderForm.getItems().size() < 1){
            model.addAttribute("addError", "Cannot create order with zero items!");
        }
        else if(!bindingResult.hasErrors()){
            try{
                orderService.saveOrder(orderForm);
                model.addAttribute("outgoingOrderForm", new OutgoingOrderForm());
            }
            catch (StockException e){
                model.addAttribute("addError", e.getMessage());
            }
        }
        else{
            if(bindingResult.hasFieldErrors("receiver")){
                model.addAttribute("addError",
                        bindingResult.getFieldError("receiver").toString()
                );
            }
        }
        return ViewNames.OUTGOING_ORDER_FORM_FRAGMENT;
    }

    @RequestMapping(value = ORDER_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getOrdersTable(@RequestParam(required = false, name = "contact") String receiverQuery,
                                 @RequestParam(required = false, name = "createdBefore")LocalDateTime createdBeforeQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model){
        addPageToModel(receiverQuery, createdBeforeQuery, pageNumber, pageSize, model);

        return ViewNames.OUTGOING_ORDER_TABLE_FRAGMENT;
    }

    @GetMapping(ORDER_DELETE_PATH)
    public String deleteOrderById(@RequestParam Long id,
                                  @RequestParam(required = false, name = "contact") String receiverQuery,
                                  @RequestParam(required = false, name = "createdBefore")LocalDateTime createdBeforeQuery,
                                  @RequestParam(defaultValue = "0") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  Model model){

        orderService.deleteById(id);
        addPageToModel(receiverQuery, createdBeforeQuery, pageNumber, pageSize, model);

        return ViewNames.OUTGOING_ORDER_TABLE_FRAGMENT;
    }


    private void addPageToModel(String receiver,
                           LocalDateTime createdBefore,
                           Integer pageNumber,
                           Integer pageSize,
                           Model model){
        Page<OutgoingOrder> ordersPage = orderService.filterOrderPage(receiver, createdBefore, pageNumber, pageSize);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("contactQuery", receiver);
        model.addAttribute("createdBeforeQuery", createdBefore);

    }

}
