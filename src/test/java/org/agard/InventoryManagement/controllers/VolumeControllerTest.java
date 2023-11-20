package org.agard.InventoryManagement.controllers;

import lombok.With;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.domain.Category;
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

@WebMvcTest(VolumeController.class)
@Import(SecurityConfig.class)
class VolumeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    VolumeService volumeService;

    @Captor
    ArgumentCaptor<Long> longArgumentCaptor;

    @Captor
    ArgumentCaptor<Volume> volumeArgumentCaptor;


    @Test
    @WithMockUser(roles = "EDITOR")
    void getVolumeTable() throws Exception{
        Mockito.when(volumeService.getAllVolumes()).thenReturn(ProductControllerTest.createMockVolumeList());

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumes"))
                .andReturn();

        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(
                ((List<Volume>)modelMap.getAttribute("volumes")).size(),
                3);
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
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 403 - User does not have access");
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
        assertEquals((Volume) modelMap.getAttribute("volume"), new Volume());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getExistingVolumeUpdateForm() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumeList().get(0);
        Mockito.when(volumeService.getById(any(Long.class))).thenReturn(Optional.ofNullable(mockVolume));

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_UPDATE_PATH)
                        .queryParam("id", mockVolume.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("volume"))
                .andReturn();

        verify(volumeService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), mockVolume.getId());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals((Volume) modelMap.getAttribute("volume"), mockVolume);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getNonExistingVolumeUpdate() throws Exception {
        Mockito.when(volumeService.getById(any(Long.class))).thenReturn(Optional.empty());
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_UPDATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verify(volumeService, times(1)).getById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), fakeId);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 404 - Object not found");
        List errorMsgList = (List) modelMap.getAttribute("errorMessageList");
        assertEquals(errorMsgList.size(), 1);
        assertEquals(errorMsgList.get(0), "Volume not found for ID: "+fakeId);
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postNewOrUpdateVolume() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumeList().get(0);

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
        assertEquals(volumeArgumentCaptor.getValue(), mockVolume);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals((Volume) modelMap.getAttribute("volume"), new Volume());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postBadVolumeUpdate() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumeList().get(0);
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
        assertEquals((Volume) modelMap.getAttribute("volume"), mockVolume);
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.volume");
        assertNotNull(bindingResult);
        assertEquals(bindingResult.getErrorCount(), 2);
        assertTrue(bindingResult.hasFieldErrors("description"));
        assertTrue(bindingResult.hasFieldErrors("valueCode"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void postVolumeNoCSRF() throws Exception {
        Volume mockVolume = ProductControllerTest.createMockVolumeList().get(0);

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
        Mockito.when(volumeService.deleteById(any(Long.class))).thenReturn(true);
        Mockito.when(volumeService.getAllVolumes()).thenReturn(ProductControllerTest.createMockVolumeList());
        Long mockId = 2L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_DELETE_PATH)
                        .queryParam("id", mockId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.VOLUME_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("volumes"))
                .andReturn();

        verify(volumeService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), mockId);
        verify(volumeService, times(1)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(
                ((List<Volume>)modelMap.getAttribute("volumes")).size(),
                3);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingVolume() throws Exception {
        Mockito.when(volumeService.deleteById(any(Long.class))).thenReturn(false);
        Long fakeId = 2L;

        MvcResult mockResult = mockMvc.perform(get(VolumeController.VOLUME_DELETE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(view().name(ViewNames.ERROR_VIEW))
                .andExpect(model().attributeExists("errorTitle", "errorMessageList"))
                .andReturn();

        verify(volumeService, times(1)).deleteById(longArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue(), fakeId);
        verify(volumeService, times(0)).getAllVolumes();

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 404 - Object not found");
        List errorMsgList = (List) modelMap.getAttribute("errorMessageList");
        assertEquals(errorMsgList.size(), 1);
        assertEquals(errorMsgList.get(0), "Volume not found for ID: "+fakeId);
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
        assertEquals(modelMap.getAttribute("errorTitle"), "HTTP 403 - User does not have access");
    }
}