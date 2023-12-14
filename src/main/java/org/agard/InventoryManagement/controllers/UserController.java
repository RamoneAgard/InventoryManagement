package org.agard.InventoryManagement.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.ItemDeleteException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.annotations.IsAdmin;
import org.agard.InventoryManagement.config.DbUserDetails;
import org.agard.InventoryManagement.config.UserRole;
import org.agard.InventoryManagement.service.UserService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@IsAdmin
public class UserController {

    public static final String USERS_PATH = "/users";

    public static final String USERS_UPDATE_PATH = USERS_PATH + "/update";

    public static final String USERS_TABLE_PATH = USERS_PATH + "/table";

    public static final String USERS_DELETE_PATH = USERS_PATH + "/delete";

    public static final String USERS_ENABLE_PATH = USERS_PATH + "/enable";

    private final UserService userService;


    @GetMapping(USERS_PATH)
    public String getUserPage(Model model){

        model.addAttribute("userForm", new UserForm());
        model.addAttribute("userRoles", UserRole.values());

        return ViewNames.USER_VIEW;
    }

    @RequestMapping(value = USERS_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getUsersTable(@RequestParam(required = false, name = "lastName") String lastNameQuery,
                                @RequestParam(required = false, name = "role") UserRole roleQuery,
                                @RequestParam(defaultValue = "0") Integer pageNumber,
                                @RequestParam(required = false) Integer pageSize,
                                Model model){
        addPageToModel(lastNameQuery, roleQuery, pageNumber, pageSize, model);

        return ViewNames.USER_TABLE_FRAGMENT;
    }


    @GetMapping(USERS_UPDATE_PATH)
    public String getUpdateForm(@RequestParam(required = false) Long id,
                                Model model){

        UserForm userForm;
        if(id == null){
            userForm = new UserForm();
        }
        else {
            try{
                userForm = userService.getFormById(id);
            }
            catch (NotFoundException e){
                model.addAttribute("addError", e.getMessage());
                userForm = new UserForm();
            }
        }

        model.addAttribute("userForm", userForm);
        model.addAttribute("userRoles", UserRole.values());

        return ViewNames.USER_UPDATE_FRAGMENT;
    }


    @PostMapping(USERS_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid UserForm userForm,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model,
                                        @AuthenticationPrincipal DbUserDetails userDetails){

        if(!bindingResult.hasErrors()){
            try{
                userService.saveUser(userForm, userDetails.getCurrentUserId());
                response.setStatus(201);
                model.addAttribute("userForm", new UserForm());
            }
            catch (NotFoundException | ItemCreationException e){
                model.addAttribute("addError", e.getMessage());
            }
            catch (RuntimeException e){
                model.addAttribute("addError", "Something went wrong, reload and try again");
            }
        }
        else {
            model.addAttribute("addError",
                    bindingResult.getFieldErrors().get(0).getField() + ": " +
                    bindingResult.getFieldErrors().get(0).getDefaultMessage()
            );
        }

        model.addAttribute("userRoles", UserRole.values());

        return ViewNames.USER_UPDATE_FRAGMENT;
    }

    @GetMapping(USERS_ENABLE_PATH)
    public String enableOrDisableUser(@RequestParam Long id,
                                      @RequestParam Boolean enable,
                                      @RequestParam(required = false, name = "lastName") String lastNameQuery,
                                      @RequestParam(required = false, name = "role") UserRole roleQuery,
                                      @RequestParam(defaultValue = "0") Integer pageNumber,
                                      @RequestParam(required = false) Integer pageSize,
                                      Model model,
                                      @AuthenticationPrincipal DbUserDetails userDetails){
        try {
            if (enable) {
                userService.enableUserById(id);
            } else {
                userService.disableUserById(id, userDetails.getCurrentUserId());
            }
        }
        catch (NotFoundException | ItemCreationException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addPageToModel(lastNameQuery, roleQuery, pageNumber, pageSize, model);

        return ViewNames.USER_TABLE_FRAGMENT;
    }

    @GetMapping(USERS_DELETE_PATH)
    public String deleteUserById(@RequestParam Long id,
                                 @RequestParam(required = false, name = "lastName") String lastNameQuery,
                                 @RequestParam(required = false, name = "role") UserRole roleQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model,
                                 @AuthenticationPrincipal DbUserDetails userDetails){

        try{
            userService.deleteById(id, userDetails.getCurrentUserId());
        }
        catch (NotFoundException | ItemDeleteException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addPageToModel(lastNameQuery, roleQuery, pageNumber, pageSize, model);

        return ViewNames.USER_TABLE_FRAGMENT;
    }


    private void addPageToModel(String lastName,
                                UserRole role,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){

        Page<UserForm> userPage = userService.filterUserPage(lastName, role, pageNumber, pageSize);

        model.addAttribute("userPage", userPage);
        model.addAttribute("lastNameQuery", lastName);
        model.addAttribute("roleQuery", role);
    }


}
