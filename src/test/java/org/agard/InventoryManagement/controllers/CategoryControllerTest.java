package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.util.ViewNames;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

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

    @MockBean(name = "outgoingOrderController")
    OutgoingOrderController outgoingOrderController;
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
        assertEquals((Category) modelMap.getAttribute("category"), new Category());
        assertEquals((Volume) modelMap.getAttribute("volume"), new Volume());
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
        Mockito.when(categoryService.getAllCategories()).thenReturn(ProductControllerTest.createMockCategoryList());

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categories"))
                .andReturn();

        verify(categoryService, times(1)).getAllCategories();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(
                ((List<Category>)modelMap.getAttribute("categories")).size(),
                3);
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
        assertEquals((Category) modelMap.getAttribute("category"), new Category());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getExistingCategoryUpdateForm() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryList().get(0);
        Mockito.when(categoryService.getById(any(Long.class))).thenReturn(mockCategory);

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_UPDATE_PATH)
                .queryParam("id", mockCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("category"))
                .andReturn();

        verify(categoryService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), mockCategory.getId());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals((Category) modelMap.getAttribute("category"), mockCategory);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNonExistingCategoryUpdate() throws Exception {
        Mockito.when(categoryService.getById(any(Long.class))).thenThrow(NotFoundException.class);
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_UPDATE_PATH)
                .queryParam("id", fakeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verify(categoryService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), fakeId);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 404 - Object not found");
        List errorMsgList = (List) modelMap.getAttribute("errorMessageList");
        assertEquals(errorMsgList.size(), 1);
        //assertEquals(errorMsgList.get(0), "Category not found for ID: "+fakeId);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postNewOrUpdateCategory() throws Exception{
        Category mockCategory = ProductControllerTest.createMockCategoryList().get(0);

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
        assertEquals(categoryArgumentCaptor.getValue(), mockCategory);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals((Category) modelMap.getAttribute("category"), new Category());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postBadCategoryUpdate() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryList().get(0);
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
        assertEquals((Category) modelMap.getAttribute("category"), mockCategory);
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.category");
        assertNotNull(bindingResult);
        assertEquals(bindingResult.getErrorCount(), 1);
        assertTrue(bindingResult.hasFieldErrors("name"));

    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postCategoryNoCSRF() throws Exception {
        Category mockCategory = ProductControllerTest.createMockCategoryList().get(0);

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
        //Mockito.when(categoryService.deleteById(any(Long.class))).thenReturn(true);
        Mockito.when(categoryService.getAllCategories()).thenReturn(ProductControllerTest.createMockCategoryList());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_DELETE_PATH)
                .queryParam("id", mockId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.CATEGORY_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("categories"))
                .andReturn();

        verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), mockId);
        verify(categoryService, times(1)).getAllCategories();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(
                ((List<Category>)modelMap.getAttribute("categories")).size(),
                3);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingCategory() throws Exception{
        Mockito.doThrow(NotFoundException.class).when(categoryService).deleteById(any(Long.class));
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(CategoryController.CATEGORY_DELETE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), fakeId);
        verify(categoryService, times(0)).getAllCategories();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 404 - Object not found");
        List errorMsgList = (List) modelMap.getAttribute("errorMessageList");
        assertEquals(errorMsgList.size(), 1);
        //assertEquals(errorMsgList.get(0), "Category not found for ID: "+fakeId);
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
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 403 - User does not have access");
    }
}