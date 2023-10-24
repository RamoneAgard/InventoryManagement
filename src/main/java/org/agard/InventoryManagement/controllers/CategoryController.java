package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class CategoryController {

    public static final String CATEGORY_LIST_PATH = "/categories";
    public static final String CATEGORY_UPDATE_PATH = "/categories/update";

    private final CategoryService categoryService;

    @GetMapping(CATEGORY_LIST_PATH)
    public String getCategories(Model model){
        List<Category> categoryList = categoryService.getAllCategories();
        model.addAttribute("categories", categoryList);
        return ViewNames.CATEGORY_LIST;
    }

    @GetMapping(CATEGORY_UPDATE_PATH)
    public String getCategoryUpdate(@RequestParam(required = false) Long id,
                                    Model model){
        Category category;
        if(id == null){
            category = new Category();
        } else{
            category = categoryService.getById(id);
            if(category == null){
                throw new NotFoundException("Category not found for id: " + id);
            }
        }
        model.addAttribute("category", category);

        return ViewNames.CATEGORY_UPDATE;
    }

    public String processCreateOrUpdate(@Valid Category category,
                                        BindingResult bindingResult,
                                        Model model){
        if(bindingResult.hasErrors()){
            return ViewNames.CATEGORY_UPDATE;
        }

        categoryService.saveCategory(category);

        return "redirect:" + CATEGORY_LIST_PATH;
    }

}
