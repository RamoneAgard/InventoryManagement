package org.agard.InventoryManagement.controllers;

import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.config.DbUserDetails;
import org.agard.InventoryManagement.config.SecurityConfig;
import org.agard.InventoryManagement.config.UserRole;
import org.agard.InventoryManagement.domain.User;
import org.agard.InventoryManagement.mappers.UserFormMapper;
import org.agard.InventoryManagement.mappers.UserFormMapperImpl;
import org.agard.InventoryManagement.service.UserService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = UserControllerTest.TestSecurityContextConfig.class)
@interface WithMockDbUser{
    long idValue() default  10L;
}


@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserFormMapperImpl.class})
class UserControllerTest {

    //Creating a mock security context to access mock principle during controller testing
    static class TestSecurityContextConfig implements WithSecurityContextFactory<WithMockDbUser>{
        @Override
        public SecurityContext createSecurityContext(WithMockDbUser annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            DbUserDetails principle = new DbUserDetails(createMockUser(annotation.idValue()));
            Authentication auth = UsernamePasswordAuthenticationToken.authenticated(principle, principle.getPassword(), principle.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }

        private User createMockUser(long idVal){
            return User.builder()
                    .id(idVal)
                    .username("testAdmin")
                    .password("password")
                    .firstName("Tessa")
                    .lastName("Adams")
                    .role(UserRole.ADMIN.name)
                    .build();
        }
    }


    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserFormMapper formMapper;

    @MockBean
    UserService userService;

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
    ArgumentCaptor<UserForm> formArgumentCaptor;

    private Page<UserForm> createMockUserPage(){

        UserForm u1 = UserForm.builder()
                .id(1L)
                .username("@admin")
                .firstName("John")
                .lastName("Bossy")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        UserForm u2 = UserForm.builder()
                .id(2L)
                .username("@editor")
                .firstName("Eddy")
                .lastName("Torri")
                .role(UserRole.EDITOR)
                .enabled(true)
                .build();

        UserForm u3 = UserForm.builder()
                .id(3L)
                .username("@user")
                .firstName("Lou")
                .lastName("Stewart")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        return new PageImpl<>(Arrays.asList(u1, u2, u3));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersPage() throws Exception{

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_VIEW))
                .andExpect(model().attributeExists("userForm", "userRoles"))
                .andReturn();

        verifyNoInteractions(userService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new UserForm(), modelMap.getAttribute("userForm"));
        assertArrayEquals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles"));
    }


    @Test
    void getUsersPageUnauthorized() throws Exception{

        mockMvc.perform(get(UserController.USERS_PATH))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void getUsersPageForbidden() throws Exception{

        mockMvc.perform(get(UserController.USERS_PATH))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl(CustomErrorController.RESOURCE_DENIED_PATH));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersTable() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_TABLE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage"))
                .andReturn();

        verify(userService, times(1)).filterUserPage(null, null, 0 , null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertNull(modelMap.getAttribute("lastNameQuery"));
        assertNull(modelMap.getAttribute("roleQuery"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersTableWithPostFilters() throws Exception {
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        String lastNameQuery = "davis";
        Integer pageSizeQuery = 2;

        MvcResult mockResult = mockMvc.perform(post(UserController.USERS_TABLE_PATH)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(ProductControllerTest.createPostFormData(
                        "role", UserRole.USER.name,
                        "lastName", lastNameQuery,
                        "pageSize", pageSizeQuery.toString()
                )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "roleQuery", "lastNameQuery"))
                .andReturn();

        verify(userService, times(1)).filterUserPage(lastNameQuery, UserRole.USER, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(UserRole.USER, modelMap.getAttribute("roleQuery"));
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNewUserUpdateForm() throws Exception{
        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_UPDATE_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "userRoles"))
                .andReturn();

        verifyNoInteractions(userService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new UserForm(), modelMap.getAttribute("userForm"));
        assertArrayEquals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getExistingUserUpdateForm() throws Exception{
        UserForm mockUserForm = createMockUserPage().getContent().get(0);
        Mockito.when(userService.getFormById(any()))
                .thenReturn(mockUserForm);

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_UPDATE_PATH)
                .queryParam("id", mockUserForm.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "userRoles"))
                .andReturn();

        verify(userService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(mockUserForm.getId(), longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockUserForm, modelMap.getAttribute("userForm"));
        assertArrayEquals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNonExistingUserForm() throws Exception{
        String mockExceptionMessage = "User not found for given ID";
        Mockito.when(userService.getFormById(any()))
                .thenThrow(new NotFoundException(mockExceptionMessage));
        Long fakeId = 5L;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_UPDATE_PATH)
                        .queryParam("id", fakeId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "addError", "userRoles"))
                .andReturn();

        verify(userService, times(1)).getFormById(longArgumentCaptor.capture());
        assertEquals(fakeId, longArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new UserForm(), modelMap.getAttribute("userForm"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
    }


    @Test
    @WithMockDbUser()
    void postNewOrUpdateUser() throws Exception{
        UserForm mockUserForm = createMockUserPage().getContent().get(0);
        mockUserForm.setEnabled(false);

        MvcResult mockResult = mockMvc.perform(post(UserController.USERS_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockUserForm.getId().toString(),
                                "firstName", mockUserForm.getFirstName(),
                                "lastName", mockUserForm.getLastName(),
                                "username", mockUserForm.getUsername(),
                                "role", mockUserForm.getRole().name
                        )))
                .andExpect(status().isCreated())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "userRoles"))
                .andReturn();

        verify(userService, times(1)).saveUser(formArgumentCaptor.capture(), any());
        assertEquals(mockUserForm, formArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(new UserForm(), modelMap.getAttribute("userForm"));
        assertArrayEquals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles"));
    }

    @Test
    @WithMockDbUser()
    void postBadUserForm() throws Exception{
        UserForm mockUserForm = createMockUserPage().getContent().get(0);
        mockUserForm.setEnabled(false);
        mockUserForm.setUsername("ab");
        mockUserForm.setFirstName("");

        MvcResult mockResult = mockMvc.perform(post(UserController.USERS_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockUserForm.getId().toString(),
                                "firstName", mockUserForm.getFirstName(),
                                "lastName", mockUserForm.getLastName(),
                                "username", mockUserForm.getUsername(),
                                "role", mockUserForm.getRole().name
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "userRoles", "addError"))
                .andReturn();

        verifyNoInteractions(userService);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        BindingResult bindingResult = (BindingResult) modelMap.getAttribute("org.springframework.validation.BindingResult.userForm");
        assertNotNull(bindingResult);
        assertEquals(2, bindingResult.getErrorCount());
        assertTrue(bindingResult.hasFieldErrors("firstName"));
        assertTrue(bindingResult.hasFieldErrors("username"));
        assertEquals(mockUserForm, modelMap.getAttribute("userForm"));
        assertTrue(Arrays.equals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles")));
    }

    @Test
    @WithMockDbUser
    void postFormWithCreationException() throws Exception{
        String mockExceptionMessage = "Username already exists";
        Mockito.doThrow(new ItemCreationException(mockExceptionMessage))
                .when(userService).saveUser(any(), any());

        UserForm mockUserForm = createMockUserPage().getContent().get(0);
        mockUserForm.setEnabled(false);
        mockUserForm.setUsername("duplicate");

        MvcResult mockResult = mockMvc.perform(post(UserController.USERS_UPDATE_PATH)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(ProductControllerTest.createPostFormData(
                                "id", mockUserForm.getId().toString(),
                                "firstName", mockUserForm.getFirstName(),
                                "lastName", mockUserForm.getLastName(),
                                "username", mockUserForm.getUsername(),
                                "role", mockUserForm.getRole().name
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_UPDATE_FRAGMENT))
                .andExpect(model().attributeExists("userForm", "userRoles", "addError"))
                .andReturn();

        verify(userService, times(1)).saveUser(formArgumentCaptor.capture(), any());
        assertEquals(mockUserForm, formArgumentCaptor.getValue());

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        assertEquals(mockExceptionMessage, modelMap.getAttribute("addError"));
        assertEquals(mockUserForm, modelMap.getAttribute("userForm"));
        assertArrayEquals(UserRole.values(), (UserRole[]) modelMap.getAttribute("userRoles"));
    }

    @Test
    @WithMockDbUser
    void getEnableUser() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 4L;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_ENABLE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("role", UserRole.USER.name)
                        .queryParam("enable", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "roleQuery"))
                .andReturn();

        verify(userService, times(1)).enableUserById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(userService, times(0)).disableUserById(any(), any());

        verify(userService, times(1)).filterUserPage(null, UserRole.USER, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(UserRole.USER, modelMap.getAttribute("roleQuery"));
    }

    @Test
    @WithMockDbUser
    void getEnableNonExistingUser() throws Exception{
        String mockExceptionMessage = "User not found for given Id";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(userService).enableUserById(any());
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 4L;
        String lastNameQuery = "smith";

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_ENABLE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("lastName", lastNameQuery)
                        .queryParam("enable", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "lastNameQuery", "tableError"))
                .andReturn();

        verify(userService, times(1)).enableUserById(longArgumentCaptor.capture());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(userService, times(0)).disableUserById(any(), any());

        verify(userService, times(1)).filterUserPage(lastNameQuery, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
    }

    @Test
    @WithMockDbUser
    void getDisableUser() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 4L;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_ENABLE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("role", UserRole.USER.name)
                        .queryParam("enable", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "roleQuery"))
                .andReturn();

        verify(userService, times(1)).disableUserById(longArgumentCaptor.capture(), any());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(userService, times(0)).enableUserById(any());

        verify(userService, times(1)).filterUserPage(null, UserRole.USER, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(UserRole.USER, modelMap.getAttribute("roleQuery"));
    }

    @Test
    @WithMockDbUser(idValue = 3L)
    void getDisableUserWithCreationException() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 3L;
        String lastNameQuery = "smith";

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_ENABLE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("lastName", lastNameQuery)
                        .queryParam("enable", "no"))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "lastNameQuery", "tableError"))
                .andReturn();

        verify(userService, times(0)).disableUserById(any(), any());
        verify(userService, times(0)).enableUserById(any());

        verify(userService, times(1)).filterUserPage(lastNameQuery, null, 0, null);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
    }

    @Test
    @WithMockDbUser
    void deleteUserById() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 2L;
        String lastNameQuery = "Smith";
        Integer pageSizeQuery = 10;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_DELETE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("role", UserRole.USER.name)
                        .queryParam("lastName", lastNameQuery)
                        .queryParam("pageSize", pageSizeQuery.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("userPage", "lastNameQuery", "roleQuery"))
                .andReturn();

        verify(userService, times(1)).deleteById(longArgumentCaptor.capture(), any());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(userService, times(1)).filterUserPage(lastNameQuery, UserRole.USER, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(UserRole.USER, modelMap.getAttribute("roleQuery"));
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
    }

    @Test
    @WithMockDbUser
    void deleteNonExistingUser() throws Exception{
        String mockExceptionMessage = "User not found for given Id";
        Mockito.doThrow(new NotFoundException(mockExceptionMessage))
                .when(userService).deleteById(any(), any());
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 6L;
        String lastNameQuery = "brown";
        Integer pageSizeQuery = 4;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_DELETE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("role", UserRole.USER.name)
                        .queryParam("lastName", lastNameQuery)
                        .queryParam("pageSize", pageSizeQuery.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("tableError", "userPage", "lastNameQuery", "roleQuery"))
                .andReturn();

        verify(userService, times(1)).deleteById(longArgumentCaptor.capture(), any());
        assertEquals(mockId, longArgumentCaptor.getValue());

        verify(userService, times(1)).filterUserPage(lastNameQuery, UserRole.USER, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(mockExceptionMessage, modelMap.getAttribute("tableError"));
        assertEquals(UserRole.USER, modelMap.getAttribute("roleQuery"));
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
    }

    @Test
    @WithMockDbUser(idValue = 2L)
    void deleteUserWithDeleteException() throws Exception{
        Mockito.when(userService.filterUserPage(any(), any(), any(), any()))
                .thenReturn(createMockUserPage());
        Long mockId = 2L;
        String lastNameQuery = "brown";
        Integer pageSizeQuery = 5;

        MvcResult mockResult = mockMvc.perform(get(UserController.USERS_DELETE_PATH)
                        .queryParam("id", mockId.toString())
                        .queryParam("lastName", lastNameQuery)
                        .queryParam("pageSize", pageSizeQuery.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(ViewNames.USER_TABLE_FRAGMENT))
                .andExpect(model().attributeExists("tableError", "userPage", "lastNameQuery"))
                .andReturn();

        verify(userService, times(0)).deleteById(any(), any());

        verify(userService, times(1)).filterUserPage(lastNameQuery, null, 0, pageSizeQuery);

        ModelMap modelMap = mockResult.getModelAndView().getModelMap();
        Page<UserForm> userPage = (Page<UserForm>) modelMap.getAttribute("userPage");
        assertEquals(3, userPage.getNumberOfElements());
        assertEquals(lastNameQuery, modelMap.getAttribute("lastNameQuery"));
        assertNull(modelMap.getAttribute("roleQuery"));
    }
}