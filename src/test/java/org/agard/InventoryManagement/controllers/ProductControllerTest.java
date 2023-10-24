package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.service.ProductServiceImpl;
import org.agard.InventoryManagement.util.ViewNames;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    //ProductServiceImpl productServiceImpl;

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<Product> productArgumentCaptor;


    @BeforeEach
    void setUp() {
    }

    // Helper method to create a page for Product objects for testing purposes
    Page<Product> createMockProductsPage() {
        Product p1 = Product.builder()
                .id(1L)
                .name("100W Light Bulbs")
                .price(new BigDecimal(4.50))
                .cost(BigDecimal.valueOf(2.50))
                .stock(45)
                .build();

        Product p2 = Product.builder()
                .id(2L)
                .name("IPhone 12 Phone Case")
                .price(BigDecimal.valueOf(10.99))
                .cost(BigDecimal.valueOf(4.45))
                .stock(32)
                .build();
        return new PageImpl<Product>(Arrays.asList(p1, p2));
    }


    // Helper Method to URL encode content for a mock Post Form
    String createPostFormData(String... formData) throws Exception {
        if(formData.length % 2 != 0){
            throw new RuntimeException("Uneven form data parameters");
        }

        StringBuilder formEncoding = new StringBuilder();
        for(int i = 0; i< formData.length; i += 2) {
            if (i > 0) {
                formEncoding.append("&");
            }
            formEncoding.append(URLEncoder.encode(formData[i], StandardCharsets.UTF_8));
            formEncoding.append("=");
            formEncoding.append(URLEncoder.encode(formData[i + 1], StandardCharsets.UTF_8));
        }

        return formEncoding.toString();
    }


    @Test
    void getProductList() throws Exception {
        Mockito.when(productService.getProductList(any(),any(),any(),any())).thenReturn(createMockProductsPage());

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_VIEW))
                .andExpect(model().attributeExists("products", "hasNext", "hasPrevious",
                                                     "pageNumber"))
                .andReturn();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertNull(modelMap.getAttribute("name"));
        assertFalse((Boolean)modelMap.getAttribute("hasNext"));
        assertFalse((Boolean)modelMap.getAttribute("hasPrevious"));
        assertNull(modelMap.getAttribute("pageSize"));
        assertEquals(modelMap.getAttribute("pageNumber"), 1);

    }

    @Test
    void getNewProductUpdate() throws Exception{
        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_ADD_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE))
                .andExpect(model().attributeExists("product"))
                .andReturn();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Product emptyProduct = (Product) modelMap.getAttribute("product");
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getCost());
        assertNull(emptyProduct.getPrice());
        assertNull(emptyProduct.getStock());
    }

    @Test
    void getExistingProductUpdate() throws Exception{
        Product mockProduct = createMockProductsPage().getContent().get(0);
        Mockito.when(productService.getById(any())).thenReturn(mockProduct);

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_ADD_PATH)
                .queryParam("id", mockProduct.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE))
                .andExpect(model().attributeExists("product"))
                .andReturn();

        verify(productService).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), mockProduct.getId());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Product existing = (Product) modelMap.getAttribute("product");
        assertEquals(mockProduct.getId(), existing.getId());
        assertEquals(mockProduct.getName(), existing.getName());
        assertEquals(mockProduct.getCost(), existing.getCost());
        assertEquals(mockProduct.getPrice(), existing.getPrice());
        assertEquals(mockProduct.getStock(), existing.getStock());
    }

    @Test
    void getNonExistingProductUpdate() throws Exception{
        Mockito.when(productService.getById(any())).thenReturn(null);
        String fakeId = "4";

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_ADD_PATH)
                        .queryParam("id", fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verify(productService).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), Long.valueOf(fakeId));

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        List errorMsgList = (List) modelMap.getAttribute("errorMessageList");
        assertEquals(errorMsgList.size(), 1);
        assertEquals(errorMsgList.get(0), "Product not found for ID: "+fakeId);

    }

    @Test
    void postNewOrUpdateProduct() throws Exception {
        Product mockProduct = createMockProductsPage().getContent().get(0);

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_ADD_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(createPostFormData(
                        "id", mockProduct.getId().toString(),
                        "name", mockProduct.getName(),
                        "cost", mockProduct.getCost().toString(),
                        "price", mockProduct.getPrice().toString(),
                        "stock", mockProduct.getStock().toString(),
                        "version", ""
                )))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:" + ProductController.PRODUCT_PATH))
                .andReturn();

        verify(productService).saveProduct(productArgumentCaptor.capture());
        assertEquals(productArgumentCaptor.getValue(), mockProduct);
    }

    @Test
    void postBadProduct() throws Exception {
        Product mockProduct = createMockProductsPage().getContent().get(0);

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_ADD_PATH)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(createPostFormData(
                                "name", mockProduct.getName(),
                                "cost", mockProduct.getCost().toString() + "g",
                                "price", mockProduct.getPrice().toString(),
                                "stock", "-1",
                                "version", ""
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE))
                .andReturn();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.product");
        assertNotNull(bindingResult);
        assertEquals(bindingResult.getErrorCount(), 2);
        assertTrue(bindingResult.hasFieldErrors("cost"));
        assertTrue(bindingResult.hasFieldErrors("stock"));
        assertFalse(bindingResult.hasFieldErrors("name"));
    }
}