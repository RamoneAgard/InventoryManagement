package org.agard.InventoryManagement.controllers;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@RequiredArgsConstructor
@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public String getCategoryUpdate(Model model){
        return "";
    }

}
