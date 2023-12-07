package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.Volume;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VolumeController.class)
@Import(SecurityConfig.class)
class VolumeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    VolumeService volumeService;

    //for thymeleaf template processing
    @MockBean(name = "productController")
    ProductController productController;

    @MockBean(name = "categoryController")
    CategoryController categoryController;

    @MockBean(name = "outgoingOrderController")
    OutgoingOrderController outgoingOrderController;

    @MockBean(name = "receivingOrderController")
    ReceivingOrderController receivingOrderController;
    //

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<Volume> volumeArgumentCaptor;


    @Test
    @WithMockUser(roles = "EDITOR")
    void getVolumeTable() throws Exception{
        Mockito.when(volumeService.filterVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage"))
                .andReturn();

        verify(volumeService, times(1)).filterVolumePage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
        assertNull(modelMap.getAttribute("descriptionQuery"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getVolumeTableWithPostFilters() throws Exception{
        Mockito.when(volumeService.filterVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        String descriptionQuery = "750ml";
        Integer pageSizeQuery = 15;

        MvcResult mockResult = mockMvc.perform(post(VolumeController.VOLUME_TABLE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "description", descriptionQuery,
                                "pageSize", pageSizeQuery.toString()
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage", "descriptionQuery"))
                .andReturn();

        verify(volumeService, times(1)).filterVolumePage(descriptionQuery, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(descriptionQuery, modelMap.getAttribute("descriptionQuery"));
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
    }

    @Test
    @WithMockUser
    void getVolumeTableForbidden() throws Exception{
        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_TABLE_PATH))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDeletedVolumesTable() throws Exception{
        Mockito.when(volumeService.filterDeletedVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        String descriptionQuery = "750ml";

        MvcResult mockResult = mockMvc.perform(post(VolumeController.VOLUME_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(ProductControllerTest.createPostFormData(
                        "description", descriptionQuery,
                        "deleted", "true"
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage", "descriptionQuery", "deletedQuery"))
                .andReturn();

        verify(volumeService, times(1)).filterDeletedVolumePage(descriptionQuery, 0, null);
        verify(volumeService, times(0)).filterVolumePage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<Volume> volumePage = (Page<Volume>) modelMap.getAttribute("volumePage");
        assertEquals(3, volumePage.getNumberOfElements());
        assertEquals(descriptionQuery, modelMap.getAttribute("descriptionQuery"));
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getDeletedVolumeTableForbidden() throws Exception{
        String descriptionQuery = "750ml";

        MvcResult mockResult = mockMvc.perform(post(VolumeController.VOLUME_TABLE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "description", descriptionQuery,
                                "deleted", "true"
                        ))
                )
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNewVolumeUpdateForm() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_UPDATE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Volume(), (Volume) modelMap.getAttribute("volume"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getExistingVolumeUpdateForm() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumePage().getContent().get(0);
        Mockito.when(volumeService.getById(any(Long.class))).thenReturn(mockVolume);

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_UPDATE_PATH)
                        .queryParam("id", mockVolume.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume"))
                .andReturn();

        verify(volumeService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(mockVolume.getId(), longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockVolume, (Volume) modelMap.getAttribute("volume"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNonExistingVolumeUpdate() throws Exception {
        String mockExceptionMessage = "Volume not found for given ID";
        Mockito.when(volumeService.getById(any(Long.class)))
                .thenThrow(new NotFoundException(mockExceptionMessage));
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_UPDATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume", "addError"))
                .andReturn();

        verify(volumeService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Volume(), modelMap.getAttribute("volume"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postNewOrUpdateVolume() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumePage().getContent().get(0);

        MvcResult mockResult = mockMvc.perform(post(VolumeController.VOLUME_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockVolume.getId().toString(),
                                "description", mockVolume.getDescription(),
                                "valueCode", mockVolume.getValueCode().toString()
                        )))
                .andExpect(status().isCreated())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume"))
                .andReturn();

        verify(volumeService, times(1)).saveVolume(volumeArgumentCaptor.capture());
        assertEquals(mockVolume, volumeArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new Volume(), (Volume) modelMap.getAttribute("volume"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postBadVolumeUpdate() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumePage().getContent().get(0);
        mockVolume.setDescription("some vol some vol some vol some vol some vol");
        mockVolume.setValueCode(null);

        MvcResult mockResult = mockMvc.perform(post(VolumeController.VOLUME_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockVolume.getId().toString(),
                                "description", mockVolume.getDescription(),
                                "valueCode", ""
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockVolume ,(Volume) modelMap.getAttribute("volume"));
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.volume");
        assertNotNull(bindingResult);
        assertEquals(2, bindingResult.getErrorCount());
        assertTrue(bindingResult.hasFieldErrors("description"));
        assertTrue(bindingResult.hasFieldErrors("valueCode"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postVolumeNoCSRF() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumePage().getContent().get(0);

        mockMvc.perform(post(CategoryController.CATEGORY_UPDATE_PATH)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockVolume.getId().toString(),
                                "description", mockVolume.getDescription(),
                                "valueCode", mockVolume.getValueCode().toString()
                        )))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl(CustomErrorController.RESOURCE_DENIED_PATH));

        verifyNoInteractions(volumeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVolumeById() throws Exception {
        Mockito.when(volumeService.filterVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_DELETE_PATH)
                        .queryParam("id", mockId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage"))
                .andReturn();

        verify(volumeService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());
        verify(volumeService, times(1)).filterVolumePage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingVolume() throws Exception {
        String mockExceptionMessage = "Volume not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(volumeService).deleteById(any(Long.class));
        Mockito.when(volumeService.filterVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_DELETE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage", "tableError"))
                .andReturn();

        verify(volumeService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());
        verify(volumeService, times(1)).filterVolumePage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void deleteVolumeForbidden() throws Exception {
        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_DELETE_PATH)
                        .queryParam("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateVolumeById() throws Exception {
        Mockito.when(volumeService.filterDeletedVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        Long mockId = 2L;
        String descriptionQuery = "50ml";

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_REACTIVATE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("description", descriptionQuery))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage", "descriptionQuery", "deletedQuery"))
                .andReturn();

        verify(volumeService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());
        verify(volumeService, times(1)).filterDeletedVolumePage(any(), any(), any());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
        assertEquals(descriptionQuery, modelMap.getAttribute("descriptionQuery"));
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateNonExistingVolume() throws Exception {
        String mockExceptionMessage = "Volume not found for given ID";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(volumeService).activateById(any(Long.class));
        Mockito.when(volumeService.filterDeletedVolumePage(any(), any(), any()))
                .thenReturn(ProductControllerTest.createMockVolumePage());
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_REACTIVATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumePage", "tableError"))
                .andReturn();

        verify(volumeService, times(1)).activateById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());
        verify(volumeService, times(1)).filterDeletedVolumePage(null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(3,
                ((Page<Volume>)modelMap.getAttribute("volumePage")).getNumberOfElements());
        assertEquals("true", modelMap.getAttribute("deletedQuery"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void activateVolumeForbidden() throws Exception {
        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_REACTIVATE_PATH)
                        .queryParam("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verifyNoInteractions(volumeService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals("HTTP 403 - User does not have access", modelMap.getAttribute("errorTitle"));
    }

}