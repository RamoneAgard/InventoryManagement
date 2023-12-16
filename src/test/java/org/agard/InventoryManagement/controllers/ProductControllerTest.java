package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.mappers.ProductMapper;
import org.agard.InventoryManagement.mappers.ProductMapperImpl;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, ProductMapperImpl.class})
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductMapper productMapper;

    @MockBean
    ProductService productService;

    //for thymeleaf template processing
    @MockBean(name = "categoryController")
    CategoryController categoryController;

    @MockBean(name = "outgoingOrderController")
    OutgoingOrderController outgoingOrderController;

    @MockBean(name = "receivingOrderController")
    ReceivingOrderController receivingOrderController;
    //

    @MockBean
    CategoryService categoryService;

    @MockBean
    VolumeService volumeService;

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<ProductForm> productFormArgumentCaptor;


    @BeforeEach
    void setUp() {
    }

    // Helper method to create a page for Product objects for testing purposes
    public static Page<Product> createMockProductsPage() {
        List<Category> cats = createMockCategoryPage().getContent();
        List<Volume> vols = createMockVolumePage().getContent();
        Product p1 = Product.builder()
                .id(1L)
                .upc("142536475869")
                .name("New Amsterdam Peach")
                .itemCode("va-p50")
                .price(new BigDecimal(80.00))
                .cost(BigDecimal.valueOf(65.00))
                .volume(vols.get(0))
                .unitSize(120)
                .stock(45)
                .category(cats.get(0))
                .build();


        Product p2 = Product.builder()
                .id(2L)
                .upc("079685746352")
                .name("Evan Williams Original")
                .itemCode("we-o37")
                .price(BigDecimal.valueOf(90.99))
                .cost(BigDecimal.valueOf(75.45))
                .volume(vols.get(1))
                .unitSize(12)
                .stock(25)
                .category(cats.get(1))
                .build();

        Product p3 = Product.builder()
                .id(3L)
                .upc("109284056978")
                .name("Blue Moon")
                .itemCode("ab-w6p")
                .price(BigDecimal.valueOf(31.99))
                .cost(BigDecimal.valueOf(22.50))
                .volume(vols.get(2))
                .unitSize(4)
                .stock(30)
                .category(cats.get(2))
                .build();

        return new PageImpl<Product>(Arrays.asList(p1, p2, p3));
    }

    public static Page<Category> createMockCategoryPage(){

        Category c1 = Category.builder()
                        .id(1L)
                        .name("Vodka")
                        .build();

        Category c2 = Category.builder()
                        .id(2L)
                        .name("Whiskey")
                        .build();

        Category c3 = Category.builder()
                        .id(3L)
                        .name("Wheat Ale")
                        .build();

        return new PageImpl<Category>(Arrays.asList(c1, c2, c3));
    }

    public static Page<Volume> createMockVolumePage(){

        Volume v1 = Volume.builder()
                        .id(1L)
                        .description("50ml")
                        .valueCode(50)
                        .build();

        Volume v2 = Volume.builder()
                        .id(2L)
                        .description("375ml")
                        .valueCode(375)
                        .build();

        Volume v3 = Volume.builder()
                        .id(3L)
                        .description("6pk 12oz")
                        .valueCode(6012)
                        .build();

        return new PageImpl<Volume>(Arrays.asList(v1, v2, v3));
    }


    // Helper Method to URL encode content for a mock Post Form
    public static String createPostFormData(String... formData) throws Exception {
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
        System.out.println(formEncoding.toString());
        return formEncoding.toString();
    }


    @Test
    @WithMockUser
    void getProductPage() throws Exception{
        Mockito.when(volumeService.getAllVolumes()).thenReturn(createMockVolumePage().getContent());
        Mockito.when(categoryService.getAllCategories()).thenReturn(createMockCategoryPage().getContent());

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_VIEW))
                .andExpect(model().attributeExists("categories", "volumes", "productForm"))
                .andReturn();

        verifyNoInteractions(productService);
        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3, ((List<Category>)modelMap.getAttribute("categories")).size());
        assertEquals(3,  ((List<Volume>)modelMap.getAttribute("volumes")).size());
        ProductForm emptyProductForm = (ProductForm) modelMap.getAttribute("productForm");
        assertEquals(new ProductForm(), emptyProductForm);
    }

    @Test
    void UnauthorizedGetProductPage() throws Exception{

        mockMvc.perform(get(ProductController.PRODUCT_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    void getProductTable() throws Exception {
        Mockito.when(productService.filterProductPage(any(), any(), any(), any(), any()))
                .thenReturn(createMockProductsPage());

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage"))
                .andReturn();

        verify(productService, times(1)).filterProductPage(null, null, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertNull(modelMap.getAttribute("nameQuery"));
        assertNull(modelMap.getAttribute("categoriesQuery"));
        assertNull(modelMap.getAttribute("volumesQuery"));
        assertEquals(3, productPage.getNumberOfElements());
        assertFalse(productPage.hasNext());
        assertFalse(productPage.hasPrevious());
        assertEquals(0, productPage.getNumber());
    }

    @Test
    @WithMockUser
    void getProductTableWithPostFilters() throws Exception {
        Mockito.when(productService.filterProductPage(any(),any(),any(),any(),any()))
                .thenReturn(createMockProductsPage());

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_TABLE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(createPostFormData(
                                "name", "New",
                                "category", "1",
                                "category", "2",
                                "volume", "1",
                                "pageSize", "3"
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "nameQuery", "categoriesQuery", "volumesQuery"))
                .andReturn();

        verify(productService, times(1)).filterProductPage(eq("New"), any(List.class), any(List.class), eq(0), eq(3));

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertEquals(modelMap.getAttribute("nameQuery"), "New");
        assertEquals(2,
                ((List<Long>)modelMap.getAttribute("categoriesQuery")).size());
        assertEquals(1,
                ((List<Long>)modelMap.getAttribute("volumesQuery")).size());
        assertFalse(productPage.hasNext());
        assertFalse(productPage.hasPrevious());
        assertEquals(0, productPage.getNumber());
        assertEquals(3, productPage.getNumberOfElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeletedProductsTable() throws Exception{
        Mockito.when(productService.filterDeletedProductsPage(any(),any(),any(),any(),any()))
                .thenReturn(createMockProductsPage());
        String nameQuery = "new";

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(createPostFormData(
                        "name", nameQuery,
                        "deleted", "true"
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "nameQuery", "deletedQuery"))
                .andReturn();

        verify(productService, times(1)).filterDeletedProductsPage(nameQuery, null, null, 0, null);
        verify(productService, times(0)).filterProductPage(any(), any(), any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertFalse(productPage.hasNext());
        assertFalse(productPage.hasPrevious());
        assertEquals(0, productPage.getNumber());
        assertEquals(3, productPage.getNumberOfElements());
        assertEquals(nameQuery, modelMap.getAttribute("nameQuery"));
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getDeletedProductsTableForbidden() throws Exception{
        String nameQuery = "new";

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_TABLE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(createPostFormData(
                                "name", nameQuery,
                                "deleted", "true"
                        ))
                )
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(productService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = {"EDITOR"})
    void getNewProductUpdateForm() throws Exception{
        Mockito.when(categoryService.getAllCategories()).thenReturn(createMockCategoryPage().getContent());
        Mockito.when(volumeService.getAllVolumes()).thenReturn(createMockVolumePage().getContent());

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_UPDATE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("productForm", "categories", "volumes"))
                .andReturn();

        verifyNoInteractions(productService);
        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ProductForm emptyProductForm = (ProductForm) modelMap.getAttribute("productForm");
        List<Category> categoryList = (List<Category>) modelMap.getAttribute("categories");
        assertEquals(3, categoryList.size());
        List<Volume> volumeList = (List<Volume>) modelMap.getAttribute("volumes");
        assertEquals(3, volumeList.size());
        assertEquals(new ProductForm(), emptyProductForm);
    }

    @Test
    @WithMockUser(roles = {"EDITOR"})
    void getExistingProductUpdate() throws Exception{
        ProductForm mockProductForm = productMapper.productToProductForm(
                createMockProductsPage().getContent().get(0)
        );
        Mockito.when(productService.getFormById(any())).thenReturn(mockProductForm);

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_UPDATE_PATH)
                .queryParam("id", mockProductForm.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("productForm", "categories", "volumes"))
                .andReturn();

        verify(productService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(mockProductForm.getId(), longArgumentCaptor.getValue());

        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ProductForm existing = (ProductForm) modelMap.getAttribute("productForm");
        assertEquals(mockProductForm, existing);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNonExistingProductUpdate() throws Exception{
        String mockExceptionMessage = "Product not found for given ID";
        Mockito.when(productService.getFormById(any()))
                .thenThrow(new NotFoundException(mockExceptionMessage));
        String fakeId = "4";

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_UPDATE_PATH)
                        .queryParam("id", fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("productForm", "addError", "categories", "volumes"))
                .andReturn();

        verify(productService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(Long.valueOf(fakeId), longArgumentCaptor.getValue());

        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new ProductForm(), modelMap.getAttribute("productForm"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postNewOrUpdateProduct() throws Exception {
        ProductForm mockProductForm = productMapper.productToProductForm(
                createMockProductsPage().getContent().get(0)
        );

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_UPDATE_PATH)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(createPostFormData(
                        "id", mockProductForm.getId().toString(),
                        "upc", mockProductForm.getUpc(),
                        "itemCode", mockProductForm.getItemCode(),
                        "name", mockProductForm.getName(),
                        "categoryId", mockProductForm.getCategoryId().toString(),
                        "volumeId", mockProductForm.getVolumeId().toString(),
                        "cost", mockProductForm.getCost().toString(),
                        "price", mockProductForm.getPrice().toString(),
                        "unitSize", mockProductForm.getUnitSize().toString(),
                        "stock", mockProductForm.getStock().toString()
                )))
                .andExpect(status().isCreated())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("productForm", "categories", "volumes"))
                .andReturn();

        verify(productService, times(1)).saveProduct(productFormArgumentCaptor.capture());
        assertEquals(mockProductForm, productFormArgumentCaptor.getValue());

        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        ProductForm emptyProductForm = (ProductForm) modelMap.getAttribute("productForm");
        assertEquals(new ProductForm(), emptyProductForm);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postBadProduct() throws Exception {
        ProductForm mockProductForm = productMapper.productToProductForm(
                createMockProductsPage().getContent().get(0)
        );

        MvcResult mockResult = mockMvc.perform(post(ProductController.PRODUCT_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(createPostFormData(
                                "id", "",
                                "upc", mockProductForm.getUpc().substring(3),
                                "itemCode", mockProductForm.getItemCode(),
                                "name", mockProductForm.getName(),
                                "categoryId", mockProductForm.getCategoryId().toString(),
                                "volumeId", "",
                                "cost", "-10.45",
                                "price", mockProductForm.getPrice().toString(),
                                "unitSize", mockProductForm.getUnitSize().toString(),
                                "stock", mockProductForm.getStock().toString()
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("productForm", "categories", "volumes"))
                .andReturn();

        verifyNoInteractions(productService);
        verify(categoryService, times(1)).getAllCategories();
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.productForm");
        assertNotNull(bindingResult);
        assertEquals(3, bindingResult.getErrorCount());
        assertTrue(bindingResult.hasFieldErrors("upc"));
        assertTrue(bindingResult.hasFieldErrors("volumeId"));
        assertTrue(bindingResult.hasFieldErrors("cost"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postProductNoCSRF() throws Exception {
        ProductForm mockProductForm = productMapper.productToProductForm(
                createMockProductsPage().getContent().get(0)
        );

        mockMvc.perform(post(ProductController.PRODUCT_UPDATE_PATH)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(createPostFormData(
                                "id", "",
                                "upc", mockProductForm.getUpc().substring(3),
                                "itemCode", mockProductForm.getItemCode(),
                                "name", mockProductForm.getName(),
                                "categoryId", mockProductForm.getCategoryId().toString(),
                                "volumeId", "",
                                "cost", "-10.45",
                                "price", mockProductForm.getPrice().toString(),
                                "unitSize", mockProductForm.getUnitSize().toString(),
                                "stock", mockProductForm.getStock().toString()
                        )))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl(CustomErrorController.RESOURCE_DENIED_PATH));

        verifyNoInteractions(productService);
        verifyNoInteractions(categoryService);
        verifyNoInteractions(volumeService);
    }

    @Test
    @WithMockUser
    void getUpdateFormForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_UPDATE_PATH)
                        .queryParam("id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(productService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductById() throws Exception{
        Mockito.when(productService.filterProductPage(any(), any(), any(), any(), any()))
                .thenReturn(createMockProductsPage());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_DELETE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("category", "1")
                        .queryParam("volume", "1")
                        .queryParam("volume", "2")
                        .queryParam("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "categoriesQuery", "volumesQuery"))
                .andReturn();

        verify(productService, times(1)).softDeleteById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(productService, times(1)).filterProductPage(eq(null), any(List.class), any(List.class), eq(0), eq(null));

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertEquals(1,
                ((List<Long>)modelMap.getAttribute("categoriesQuery")).size());
        assertEquals(2,
                ((List<Long>)modelMap.getAttribute("volumesQuery")).size());
        assertNull(modelMap.getAttribute("nameQuery"));
        assertEquals(3, productPage.getNumberOfElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNotExistingProduct() throws Exception{
        String mockExceptionMessage = "Product not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(productService).softDeleteById(any(Long.class));
        Mockito.when(productService.filterProductPage(any(), any(), any(), any(), any()))
                .thenReturn(createMockProductsPage());
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_DELETE_PATH)
                .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "tableError"))
                .andReturn();

        verify(productService, times(1)).softDeleteById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), fakeId);

        verify(productService, times(1)).filterProductPage(null, null, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertEquals(3, productPage.getNumberOfElements());
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void deleteProductForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_DELETE_PATH)
                        .queryParam("id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(productService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateProductById() throws Exception{
        Mockito.when(productService.filterDeletedProductsPage(any(), any(), any(), any(), any()))
                .thenReturn(createMockProductsPage());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_REACTIVATE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("category", "1")
                        .queryParam("volume", "1")
                        .queryParam("volume", "2")
                        .queryParam("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "categoriesQuery", "volumesQuery", "deletedQuery"))
                .andReturn();

        verify(productService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(productService, times(1)).filterDeletedProductsPage(eq(null), any(List.class), any(List.class), eq(0), eq(null));

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertEquals(0, productPage.getNumber());
        assertEquals(1,
                ((List<Long>)modelMap.getAttribute("categoriesQuery")).size());
        assertEquals(2,
                ((List<Long>)modelMap.getAttribute("volumesQuery")).size());
        assertNull(modelMap.getAttribute("nameQuery"));
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateNotExistingProduct() throws Exception{
        String mockExceptionMessage = "Product not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(productService).activateById(any(Long.class));
        Mockito.when(productService.filterDeletedProductsPage(any(), any(), any(), any(), any()))
                .thenReturn(createMockProductsPage());
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_REACTIVATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.PRODUCT_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("productPage", "tableError", "deletedQuery"))
                .andReturn();

        verify(productService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        verify(productService, times(1)).filterDeletedProductsPage(null, null, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Product> productPage = (Page<Product>) modelMap.getAttribute("productPage");
        assertEquals(3, productPage.getNumberOfElements());
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void activateProductForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(ProductController.PRODUCT_REACTIVATE_PATH)
                        .queryParam("id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(productService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

}