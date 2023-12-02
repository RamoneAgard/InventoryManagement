package org.agard.InventoryManagement.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_EDITOR')")
public class CategoryController {

    public static final String ATTRIBUTE_PATH = "/attributes";
    public static final String CATEGORY_TABLE_PATH = "/categories/table";
    public static final String CATEGORY_UPDATE_PATH = "/categories/update";
    public static final String CATEGORY_DELETE_PATH = "/categories/delete";

    private final CategoryService categoryService;


    @GetMapping(ATTRIBUTE_PATH)
    public String getAttributePage(Model model){

        model.addAttribute("category", new Category());
        model.addAttribute("volume", new Volume());

        return ViewNames.ATTRIBUTE_VIEW;
    }


    @RequestMapping(value = CATEGORY_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getCategoryTable(@RequestParam(required = false, name = "name") String nameQuery,
                                   @RequestParam(defaultValue = "0") Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize,
                                   Model model){

        addPageToModel(nameQuery, pageNumber, pageSize, model);

        return ViewNames.CATEGORY_TABLE_FRAGMENT;
    }


    @GetMapping(CATEGORY_UPDATE_PATH)
    public String getCategoryUpdate(@RequestParam(required = false) Long id,
                                    Model model){
        Category category;
        if(id == null){
            category = new Category();
        } else {
            category = categoryService.getById(id);
        }
        model.addAttribute("category", category);

        return ViewNames.CATEGORY_UPDATE_FRAGMENT;
    }

    @PostMapping(CATEGORY_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid Category category,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model){
        if(!bindingResult.hasErrors()){
            categoryService.saveCategory(category);
            response.setStatus(201);
            model.addAttribute("category", new Category());
        }

        return ViewNames.CATEGORY_UPDATE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(CATEGORY_DELETE_PATH)
    public String deleteCategoryById(@RequestParam Long id,
                                     @RequestParam(required = false, name = "name") String nameQuery,
                                     @RequestParam(defaultValue = "0") Integer pageNumber,
                                     @RequestParam(required = false) Integer pageSize,
                                     Model model){

        categoryService.deleteById(id);
        addPageToModel(nameQuery, pageNumber, pageSize, model);

        return ViewNames.CATEGORY_TABLE_FRAGMENT;
    }

    private void addPageToModel(String name,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){

        Page<Category> categoryPage = categoryService.filterCategoryPage(name, pageNumber, pageSize);

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("nameQuery", name);
    }

}
