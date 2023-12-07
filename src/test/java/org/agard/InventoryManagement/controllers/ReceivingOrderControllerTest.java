package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.OrderItemForm;
import org.agard.InventoryManagement.ViewModels.OutgoingOrderForm;
import org.agard.InventoryManagement.ViewModels.ReceivingOrderForm;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.*;
import org.agard.InventoryManagement.mappers.*;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.OrderItemService;
import org.agard.InventoryManagement.service.ReceivingOrderService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceivingOrderController.class)
@Import({SecurityConfig.class, ReceivingOrderFormMapperImpl.class, OrderItemFormMapperImpl.class, ProductMapperImpl.class})
class ReceivingOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ReceivingOrderFormMapper orderFormMapper;

    @Autowired
    OrderItemFormMapper itemFormMapper;

    @MockBean
    ReceivingOrderService orderService;

    @MockBean
    OrderItemService itemService;

    @MockBean
    CategoryService categoryService;

    @MockBean
    VolumeService volumeService;

    //for thymeleaf template processing
    @MockBean(name = "categoryController")
    CategoryController categoryController;

    @MockBean(name = "productController")
    ProductController productController;

    @MockBean(name = "outgoingOrderController")
    OutgoingOrderController outgoingOrderController;
    //

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<ReceivingOrderForm> orderFormArgumentCaptor;


    public Page<ReceivingOrder> createMockReceivingOrdersPage(){
        List<OrderItem> mockItems = createMockOrderItems();

        ReceivingOrder o1 = ReceivingOrder.builder()
                .id(1L)
                .supplier("E&J Gallo Winery")
                .build();
        o1.getItems().add(mockItems.get(0));

        ReceivingOrder o2 = ReceivingOrder.builder()
                .id(2L)
                .supplier("Heaven Hill Distillery")
                .build();
        o2.getItems().add(mockItems.get(1));

        ReceivingOrder o3 = ReceivingOrder.builder()
                .id(3L)
                .supplier("Molson Coors Beverage Company")
                .build();
        o3.getItems().add(mockItems.get(2));

        return new PageImpl<ReceivingOrder>(Arrays.asList(o1 ,o2, o3));
    }

    public List<OrderItem> createMockOrderItems(){
        List<Product> mockProducts = ProductControllerTest.createMockProductsPage().getContent();
        List<OrderItem> itemList = new ArrayList<>();
        Long mockId = 1L;

        for(Product p : mockProducts){
            itemList.add(
                    OrderItem.builder()
                            .id(mockId)
                            .product(p)
                            .price(p.getCost())
                            .quantity(10)
                            .build()
            );
            mockId += 1;
        }
        return itemList;
    }


    @Test
    @WithMockUser
    void getReceivingOrderPage() throws Exception{
        Mockito.when(categoryService.getAllCategories())
                .thenReturn(ProductControllerTest.createMockCategoryPage().getContent());
        Mockito.when(volumeService.getAllVolumes())
                .thenReturn(ProductControllerTest.createMockVolumePage().getContent());

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_PAGE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_VIEW))
                .andExpect(model().attributeExists("receivingOrderForm", "categories", "volumes"))
                .andReturn();

        verifyNoInteractions(orderService);
        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3, ((List<Category>)modelMap.getAttribute("categories")).size());
        assertEquals(3,  ((List<Volume>)modelMap.getAttribute("volumes")).size());
        assertEquals(new ReceivingOrderForm(), (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm"));
    }

    @Test
    void getReceivingOrderPageUnauthorized() throws Exception{
        mockMvc.perform(get(ReceivingOrderController.ORDER_PAGE_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    void getOrderTable() throws Exception{
        Mockito.when(orderService.filterOrderPage(any(), any(), any(), any()))
                .thenReturn(createMockReceivingOrdersPage());

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("ordersPage"))
                .andReturn();

        verify(orderService, times(1)).filterOrderPage(null, null, 0,null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertNull(modelMap.getAttribute("contactQuery"));
        assertNull(modelMap.getAttribute("createdBeforeQuery"));
        Page<ReceivingOrder> orderPage = (Page<ReceivingOrder>) modelMap.getAttribute("ordersPage");
        assertEquals(3, orderPage.getNumberOfElements());
    }

    @Test
    @WithMockUser
    void getOrderTableWithPostFilters() throws Exception{
        Mockito.when(orderService.filterOrderPage(any(), any(), any(), any()))
                .thenReturn(createMockReceivingOrdersPage());
        LocalDateTime createdBeforeQuery = LocalDateTime.now();
        String supplierQuery = "Heaven Hill";

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_TABLE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "contact", supplierQuery,
                                "createdBefore", createdBeforeQuery.toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("ordersPage", "contactQuery", "createdBeforeQuery"))
                .andReturn();

        verify(orderService, times(1)).filterOrderPage(supplierQuery, createdBeforeQuery, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<ReceivingOrder> orderPage = (Page<ReceivingOrder>) modelMap.getAttribute("ordersPage");
        assertEquals(3, orderPage.getNumberOfElements());
        assertEquals(0, orderPage.getNumber());
        assertEquals(createdBeforeQuery, (LocalDateTime) modelMap.getAttribute("createdBeforeQuery"));
        assertEquals(supplierQuery, modelMap.getAttribute("contactQuery"));
    }

    @Test
    @WithMockUser
    void getNewOrderUpdateForm() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_UPDATE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm"))
                .andReturn();

        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new ReceivingOrderForm(),
                (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm"));
    }

    @Test
    @WithMockUser
    void getExistingOrderUpdateForm() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(0)
        );
        Mockito.when(orderService.getFormById(any())).thenReturn(mockOrderForm);

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_UPDATE_PATH)
                        .queryParam("id", mockOrderForm.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm"))
                .andReturn();

        verify(orderService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(mockOrderForm.getId(), longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockOrderForm,
                (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm"));
    }

    @Test
    @WithMockUser
    void getNonExistingOrderUpdateForm() throws Exception {
        String mockExceptionMessage = "Receiving Order not found for given ID";
        Mockito.when(orderService.getFormById(any()))
                .thenThrow(new NotFoundException(mockExceptionMessage));
        Long fakeId = 4L;

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_UPDATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verify(orderService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new ReceivingOrderForm(), modelMap.getAttribute("receivingOrderForm"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser
    void addItemToOrderUpdateForm() throws Exception{
        OrderItemForm mockItemForm = itemFormMapper.OrderItemToOrderItemForm(
                createMockOrderItems().get(0)
        );
        Mockito.when(itemService.createItemFormByItemCode(any()))
                .thenReturn(mockItemForm);
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_ADD_FORM_ITEM + "?code=" + mockItemForm.getProduct().getItemCode())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm"))
                .andReturn();

        verify(itemService, times(1)).createItemFormByItemCode(mockItemForm.getProduct().getItemCode());
        verifyNoInteractions(orderService);

        mockOrderForm.getItems().add(mockItemForm);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
    }

    @Test
    @WithMockUser
    void addDuplicateItemToOrderForm() throws Exception{
        OrderItemForm mockItemForm = itemFormMapper.OrderItemToOrderItemForm(
                createMockOrderItems().get(0)
        );
        Mockito.when(itemService.createItemFormByItemCode(any()))
                .thenReturn(mockItemForm);
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(0)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_ADD_FORM_ITEM + "?code=" + mockItemForm.getProduct().getItemCode())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verify(itemService, times(1)).createItemFormByItemCode(mockItemForm.getProduct().getItemCode());
        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
        assertEquals("Cannot add duplicate products to order", modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser
    void addNotFoundItemToOrderForm() throws Exception{
        String mockExceptionString = "Product not found for item code";
        Mockito.when(itemService.createItemFormByItemCode(any()))
                .thenThrow(new NotFoundException(mockExceptionString));

        String fakeItemCode = "ab-123";
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(0)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_ADD_FORM_ITEM + "?code=" + fakeItemCode)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verify(itemService, times(1)).createItemFormByItemCode(fakeItemCode);
        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
        assertEquals(mockExceptionString, modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser
    void removeItemFromOrderForm() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_REMOVE_FORM_ITEM + "?index=0")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm"))
                .andReturn();

        verifyNoInteractions(itemService);
        verifyNoInteractions(orderService);

        mockOrderForm.getItems().remove(0);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
    }

    @Test
    @WithMockUser
    void removeOutOfBoundsItemFromOrderForm() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_REMOVE_FORM_ITEM + "?index=1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verifyNoInteractions(itemService);
        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
        assertEquals("Remove index is out of bounds", modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser
    void postNewOrUpdateOrder() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isCreated())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm"))
                .andReturn();

        verifyNoInteractions(itemService);
        verify(orderService, times(1)).saveOrder(orderFormArgumentCaptor.capture());
        assertEquals(mockOrderForm, orderFormArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(new ReceivingOrderForm(), orderForm);
    }

    @Test
    @WithMockUser
    void postBadOrderForm() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );

        mockOrderForm.setSupplier("");
        mockOrderForm.getItems().get(0).setQuantity(-1);

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier(),
                                "items[0].id", mockOrderForm.getItems().get(0).getId().toString(),
                                "items[0].product.id", mockOrderForm.getItems().get(0).getProduct().getId().toString(),
                                "items[0].product.itemCode", mockOrderForm.getItems().get(0).getProduct().getItemCode(),
                                "items[0].product.name", mockOrderForm.getItems().get(0).getProduct().getName(),
                                "items[0].product.volumeDescription", mockOrderForm.getItems().get(0).getProduct().getVolumeDescription(),
                                "items[0].product.unitSize", mockOrderForm.getItems().get(0).getProduct().getUnitSize().toString(),
                                "items[0].price", mockOrderForm.getItems().get(0).getPrice().toString(),
                                "items[0].quantity", mockOrderForm.getItems().get(0).getQuantity().toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verifyNoInteractions(itemService);
        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.receivingOrderForm");
        assertEquals(3, bindingResult.getErrorCount());
        assertEquals(bindingResult.getAllErrors().get(0).getDefaultMessage(),
                modelMap.getAttribute("addError"));
        assertTrue(bindingResult.hasFieldErrors("supplier"));
        assertTrue(bindingResult.hasFieldErrors("items[0].quantity"));
    }

    @Test
    @WithMockUser
    void postOrderFormNoItems() throws Exception{
        ReceivingOrderForm mockOrderForm = orderFormMapper.receivingOrderToReceivingOrderForm(
                createMockReceivingOrdersPage().getContent().get(1)
        );
        mockOrderForm.getItems().clear();

        MvcResult mockResult = mockMvc.perform(post(ReceivingOrderController.ORDER_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockOrderForm.getId().toString(),
                                "supplier", mockOrderForm.getSupplier()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_FORM_FRAGMENT))
                .andExpect(model().attributeExists("receivingOrderForm", "addError"))
                .andReturn();

        verifyNoInteractions(itemService);
        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ReceivingOrderForm orderForm = (ReceivingOrderForm) modelMap.getAttribute("receivingOrderForm");
        assertEquals(mockOrderForm, orderForm);
        assertEquals("Cannot create order with zero items!", modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrderById() throws Exception{
        Mockito.when(orderService.filterOrderPage(any(), any(), any(), any()))
                .thenReturn(createMockReceivingOrdersPage());
        Long mockId = 2L;
        String supplierQuery = "Gallo";
        LocalDateTime createdBeforeQuery = LocalDateTime.now();

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_DELETE_PATH)
                    .queryParam("id", mockId.toString())
                    .queryParam("contact", supplierQuery)
                    .queryParam("createdBefore", createdBeforeQuery.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("ordersPage", "contactQuery", "createdBeforeQuery"))
                .andReturn();

        verify(orderService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(orderService, times(1)).filterOrderPage(supplierQuery, createdBeforeQuery, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<ReceivingOrder> orderPage = (Page<ReceivingOrder>) modelMap.getAttribute("ordersPage");
        assertEquals(3, orderPage.getNumberOfElements());
        assertEquals(supplierQuery, modelMap.getAttribute("contactQuery"));
        assertEquals(createdBeforeQuery, modelMap.getAttribute("createdBeforeQuery"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingOrder() throws Exception{
        String mockExceptionMessage = "Order not found";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(orderService).deleteById(any());
        Mockito.when(orderService.filterOrderPage(any(), any(), any(), any()))
                .thenReturn(createMockReceivingOrdersPage());
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_DELETE_PATH)
                    .queryParam("id", fakeId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.RECEIVING_ORDER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("ordersPage", "tableError"))
                .andReturn();

        verify(orderService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        verify(orderService, times(1)).filterOrderPage(null, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<ReceivingOrder> orderPage = (Page<ReceivingOrder>) modelMap.getAttribute("ordersPage");
        assertEquals(3, orderPage.getNumberOfElements());
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void deleteOrderForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(ReceivingOrderController.ORDER_DELETE_PATH)
                .queryParam("id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(orderService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }
}