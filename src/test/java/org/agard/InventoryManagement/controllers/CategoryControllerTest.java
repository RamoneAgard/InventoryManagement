package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CategoryService categoryService;

    //for thymeleaf template processing
    @MockBean(name = "productController")
    ProductController productController;

    @MockBean(name = "volumeController")
    VolumeController volumeController;

    @MockBean(name = "outgoingOrderController")
    OutgoingOrderController outgoingOrderController;

    @MockBean(name = "receivingOrderController")
    ReceivingOrderController receivingOrderController;
    //

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<Category> categoryArgumentCaptor;

    @Test
    @WithMockUser(roles = "EDITOR")
    void getAttributePage() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(CategoryController.ATTRIBUTE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.ATTRIBUTE_VIEW))
                .andExpect(model().attributeExists("category", "volume"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Category(), (Category) modelMap.getAttribute("category"));
        assertEquals(new Volume(), (Volume) modelMap.getAttribute("volume"));
    }

    @Test
    void getAttributePageUnauthorized() throws Exception{

        mockMvc.perform(get(CategoryController.ATTRIBUTE_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    void getAttributePageForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(CategoryController.ATTRIBUTE_PATH))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl(CustomErrorController.RESOURCE_DENIED_PATH))
                .andReturn();

        verifyNoInteractions(categoryService);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getCategoryTable() throws Exception {
        Mockito.when(categoryService.filterCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage"))
                .andReturn();

        verify(categoryService, times(1)).filterCategoryPage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Category>)modelMap.getAttribute("categoryPage")).getNumberOfElements());
        assertNull(modelMap.getAttribute("nameQuery"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getCategoryTableWithPostFilters() throws Exception{
        Mockito.when(categoryService.filterCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());

        String nameQuery = "Vodka";
        Integer pageSizeQuery = 14;

        MvcResult mockResult = mockMvc.perform(post(CategoryController.CATEGORY_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(ProductControllerTest.createPostFormData(
                        "name", nameQuery,
                        "pageSize", pageSizeQuery.toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage", "nameQuery"))
                .andReturn();

        verify(categoryService, times(1)).filterCategoryPage(nameQuery, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(nameQuery, modelMap.getAttribute("nameQuery"));
        assertEquals(3,
                ((Page<Category>) modelMap.getAttribute("categoryPage")).getNumberOfElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeletedCategoryTable() throws Exception{
        Mockito.when(categoryService.filterDeletedCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());
        String nameQuery = "ale";

        MvcResult mockResult = mockMvc.perform(post(CategoryController.CATEGORY_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(ProductControllerTest.createPostFormData(
                        "name", nameQuery,
                        "deleted", "true"
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage", "nameQuery", "deletedQuery"))
                .andReturn();

        verify(categoryService, times(1)).filterDeletedCategoryPage(nameQuery, 0, null);
        verify(categoryService, times(0)).filterCategoryPage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Category> categoryPage = (Page<Category>) modelMap.getAttribute("categoryPage");
        assertEquals(3, categoryPage.getNumberOfElements());
        assertEquals(nameQuery, modelMap.getAttribute("nameQuery"));
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getDeletedCategoryTableForbidden() throws Exception{

        MvcResult mockResult = mockMvc.perform(post(CategoryController.CATEGORY_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(ProductControllerTest.createPostFormData(
                        "deleted", "true"
                        ))
                )
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNewCategoryUpdateForm() throws Exception {

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_UPDATE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Category(), (Category) modelMap.getAttribute("category"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getExistingCategoryUpdateForm() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryPage().getContent().get(0);
        Mockito.when(categoryService.getById(any(Long.class))).thenReturn(mockCategory);

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_UPDATE_PATH)
                .queryParam("id", mockCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category"))
                .andReturn();

        verify(categoryService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(mockCategory.getId(), longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockCategory, (Category) modelMap.getAttribute("category"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNonExistingCategoryUpdate() throws Exception {
        String mockExceptionMessage = "Category not found for given ID";
        Mockito.when(categoryService.getById(any(Long.class)))
                .thenThrow(new NotFoundException(mockExceptionMessage));
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_UPDATE_PATH)
                .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category", "addError"))
                .andReturn();

        verify(categoryService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Category(), modelMap.getAttribute("category"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postNewOrUpdateCategory() throws Exception{
        Category mockCategory = ProductControllerTest.createMockCategoryPage().getContent().get(0);

        MvcResult mockResult = mockMvc.perform(post(CategoryController.CATEGORY_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockCategory.getId().toString(),
                                "name", mockCategory.getName()
                        )))
                .andExpect(status().isCreated())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category"))
                .andReturn();

        verify(categoryService, times(1)).saveCategory(categoryArgumentCaptor.capture());
        assertEquals(mockCategory, categoryArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Category(), (Category) modelMap.getAttribute("category"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postBadCategoryUpdate() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryPage().getContent().get(0);
        mockCategory.setName("ca");

        MvcResult mockResult = mockMvc.perform(post(CategoryController.CATEGORY_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockCategory.getId().toString(),
                                "name", mockCategory.getName()
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockCategory, (Category) modelMap.getAttribute("category"));
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.category");
        assertNotNull(bindingResult);
        assertEquals(1, bindingResult.getErrorCount());
        assertTrue(bindingResult.hasFieldErrors("name"));

    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postCategoryNoCSRF() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryPage().getContent().get(0);

        mockMvc.perform(post(CategoryController.CATEGORY_UPDATE_PATH)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockCategory.getId().toString(),
                                "name", mockCategory.getName()
                        )))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl(CustomErrorController.RESOURCE_DENIED_PATH));

        verifyNoInteractions(categoryService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategoryById() throws Exception {
        Mockito.when(categoryService.filterCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_DELETE_PATH)
                .queryParam("id", mockId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage"))
                .andReturn();

        verify(categoryService, times(1)).softDeleteById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());
        verify(categoryService, times(1)).filterCategoryPage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Category>)modelMap.getAttribute("categoryPage")).getNumberOfElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingCategory() throws Exception{
        String mockExceptionMessage = "Category not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(categoryService).softDeleteById(any(Long.class));
        Mockito.when(categoryService.filterCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_DELETE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage", "tableError"))
                .andReturn();

        verify(categoryService, times(1)).softDeleteById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());
        verify(categoryService, times(1)).filterCategoryPage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Category>)modelMap.getAttribute("categoryPage")).getNumberOfElements());
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void deleteCategoryForbidden() throws Exception {

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_DELETE_PATH)
                        .queryParam("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCategoryById() throws Exception {
        Mockito.when(categoryService.filterDeletedCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_REACTIVATE_PATH)
                        .queryParam("id", mockId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage", "deletedQuery"))
                .andReturn();

        verify(categoryService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());
        verify(categoryService, times(1)).filterDeletedCategoryPage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Category>)modelMap.getAttribute("categoryPage")).getNumberOfElements());
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateNonExistingCategory() throws Exception{
        String mockExceptionMessage = "Category not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(categoryService).activateById(any(Long.class));
        Mockito.when(categoryService.filterDeletedCategoryPage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockCategoryPage());
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_REACTIVATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categoryPage", "tableError"))
                .andReturn();

        verify(categoryService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());
        verify(categoryService, times(1)).filterDeletedCategoryPage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Category>)modelMap.getAttribute("categoryPage")).getNumberOfElements());
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void activateCategoryForbidden() throws Exception {

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_REACTIVATE_PATH)
                        .queryParam("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(categoryService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }
}