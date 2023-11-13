package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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


    @GetMapping(CATEGORY_TABLE_PATH)
    public String getCategoryTable(Model model){


        model.addAttribute("categories",
                categoryService.getAllCategories());

        return ViewNames.CATEGORY_TABLE_FRAGMENT;
    }

    // May integrate this with getCategories method so form and list are in same view
    @GetMapping(CATEGORY_UPDATE_PATH)
    public String getCategoryUpdate(@RequestParam(required = false) Long id,
                                    Model model){
        Category category;
        if(id == null){
            category = new Category();
        } else {
            category = categoryService.getById(id);
            if(category == null){
                throw new NotFoundException("Category not found for ID: " + id);
            }
        }
        model.addAttribute("category", category);

        return ViewNames.CATEGORY_UPDATE_FRAGMENT;
    }

    @PostMapping(CATEGORY_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid Category category,
                                        BindingResult bindingResult,
                                        Model model){
        if(!bindingResult.hasErrors()){
            categoryService.saveCategory(category);
            model.addAttribute("category", new Category());
        }

        return ViewNames.CATEGORY_UPDATE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(CATEGORY_DELETE_PATH)
    public String deleteCategoryById(@RequestParam Long id, Model model){
        if(categoryService.deleteById(id)){
            model.addAttribute("categories",
                    categoryService.getAllCategories());
            return ViewNames.CATEGORY_TABLE_FRAGMENT;
        }
        throw new NotFoundException("Category not found for ID: " + id);
    }

}
