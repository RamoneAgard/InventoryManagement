package org.agard.InventoryManagement.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.Exceptions.StockException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.annotations.IsAdmin;
import org.agard.InventoryManagement.annotations.IsUser;
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
@IsUser
public class ReceivingOrderController {

    public static final String ORDER_PAGE_PATH = "/inorders";

    public static final String ORDER_UPDATE_PATH = ORDER_PAGE_PATH + "/update";

    public static final String ORDER_ADD_FORM_ITEM = ORDER_PAGE_PATH + "/update/add";

    public static final String ORDER_REMOVE_FORM_ITEM = ORDER_PAGE_PATH + "/update/remove";

    public static final String ORDER_TABLE_PATH = ORDER_PAGE_PATH + "/table";

    public static final String ORDER_DELETE_PATH = ORDER_PAGE_PATH + "/delete";

    private final ReceivingOrderService orderService;

    private final OrderItemService itemService;

    private final CategoryService categoryService;

    private final VolumeService volumeService;

    @GetMapping(ORDER_PAGE_PATH)
    public String getOutOrdersPage(Model model){

        model.addAttribute("receivingOrderForm", new ReceivingOrderForm());
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
            try{
                orderForm = orderService.getFormById(id);
            }
            catch (NotFoundException e){
                model.addAttribute("addError", e.getMessage());
                orderForm = new ReceivingOrderForm();
            }
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

        model.addAttribute("receivingOrderForm", orderForm);

        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_REMOVE_FORM_ITEM)
    public String removeItemFromForm(ReceivingOrderForm orderForm,
                                     @RequestParam(name = "index") Integer index,
                                     Model model){

        try{
            orderForm.getItems().remove(index.intValue());
        }
        catch (IndexOutOfBoundsException e){
            model.addAttribute("addError", "Remove index is out of bounds");
        }

        model.addAttribute("receivingOrderFrom", orderForm);

        return ViewNames.RECEIVING_ORDER_FORM_FRAGMENT;
    }

    @PostMapping(ORDER_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid ReceivingOrderForm orderForm,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model){

        if(orderForm.getItems().size() < 1){
            model.addAttribute("addError", "Cannot create order with zero items!");
        }
        else if(!bindingResult.hasErrors()){
            try{
                orderService.saveOrder(orderForm);
                model.addAttribute("receivingOrderForm", new ReceivingOrderForm());
                response.setStatus(201);
            }
            catch (StockException | NotFoundException e){
                model.addAttribute("addError", e.getMessage());
            }
        }
        else{
            model.addAttribute("addError",
                    bindingResult.getFieldErrors().get(0).getField() + ": " +
                            bindingResult.getFieldErrors().get(0).getDefaultMessage()
            );
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

    @IsAdmin
    @GetMapping(ORDER_DELETE_PATH)
    public String deleteOrderById(@RequestParam Long id,
                                  @RequestParam(required = false, name = "contact") String supplierQuery,
                                  @RequestParam(required = false, name = "createdBefore")LocalDateTime createdBeforeQuery,
                                  @RequestParam(defaultValue = "0") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  Model model){

        try{
            orderService.deleteById(id);
        }
        catch (StockException | NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

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
