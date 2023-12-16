package org.agard.InventoryManagement.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.annotations.IsAdmin;
import org.agard.InventoryManagement.annotations.IsEditor;
import org.agard.InventoryManagement.annotations.IsUser;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@IsUser
public class ProductController {

    public static final String PRODUCT_PATH = "/products";

    public static final String PRODUCT_UPDATE_PATH = PRODUCT_PATH + "/update";

    public static final String PRODUCT_DELETE_PATH = PRODUCT_PATH + "/delete";

    public static final String PRODUCT_TABLE_PATH = PRODUCT_PATH + "/table";

    public static final String PRODUCT_REACTIVATE_PATH = PRODUCT_PATH + "/reactivate";

    private final ProductService productService;

    private final CategoryService categoryService;

    private final VolumeService volumeService;


    @GetMapping({"/", PRODUCT_PATH})
    public String productList(Model model){

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);
        model.addAttribute("productForm", new ProductForm());

        return ViewNames.PRODUCT_VIEW;
    }

    @RequestMapping(value = PRODUCT_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getProductTable(@RequestParam(required = false, name = "name") String nameQuery,
                                  @RequestParam(required = false, name = "category") List<Long> categoriesQuery,
                                  @RequestParam(required = false, name = "volume") List<Long> volumesQuery,
                                  @RequestParam(defaultValue = "0") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  @RequestParam(defaultValue = "product") String orderType,
                                  Model model){

        addPageToModel(nameQuery, categoriesQuery, volumesQuery, pageNumber, pageSize, model);

        if(orderType.equals("out") || orderType.equals("in")){
            model.addAttribute("orderType", orderType);
        }
        else{
            model.addAttribute("orderType", null);
        }

        return ViewNames.PRODUCT_TABLE_FRAGMENT;
    }


    @IsAdmin
    @RequestMapping(value = PRODUCT_TABLE_PATH, params = "deleted", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDeletedProductTable(@RequestParam(required = false, name = "name") String nameQuery,
                                  @RequestParam(required = false, name = "category") List<Long> categoriesQuery,
                                  @RequestParam(required = false, name = "volume") List<Long> volumesQuery,
                                  @RequestParam(defaultValue = "0") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  @RequestParam(defaultValue = "false" ,name = "deleted") Boolean deleted,
                                  Model model){

        if(deleted){
            addDeletedPageToModel(nameQuery, categoriesQuery, volumesQuery, pageNumber, pageSize, model);
            return ViewNames.PRODUCT_TABLE_FRAGMENT;
        }
        addPageToModel(nameQuery, categoriesQuery, volumesQuery, pageNumber, pageSize, model);

        return ViewNames.PRODUCT_TABLE_FRAGMENT;
    }


    @IsEditor
    @GetMapping(PRODUCT_UPDATE_PATH)
    public String getUpdateForm(Model model,
                                @RequestParam(required = false) Long id){
        ProductForm productForm;
        if(id == null){
            productForm = new ProductForm();
        }
        else {
            try{
                productForm = productService.getFormById(id);
            }
            catch (NotFoundException e){
                model.addAttribute("addError", e.getMessage());
                productForm = new ProductForm();
            }
        }

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return ViewNames.PRODUCT_UPDATE_FRAGMENT;
    }


    @IsEditor
    @PostMapping(PRODUCT_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid ProductForm productForm,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model){

        if(!bindingResult.hasErrors()){
            try{
                productService.saveProduct(productForm);
                response.setStatus(201);
                model.addAttribute("productForm", new ProductForm());
            }
            catch (NotFoundException | ItemCreationException e){
                model.addAttribute("addError", e.getMessage());
            }
        }
        else{
            model.addAttribute("addError",
                    bindingResult.getFieldErrors().get(0).getField() + ": " +
                            bindingResult.getFieldErrors().get(0).getDefaultMessage()
            );
        }

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return ViewNames.PRODUCT_UPDATE_FRAGMENT;
    }


    @IsAdmin
    @GetMapping(PRODUCT_DELETE_PATH)
    public String deleteProductById(@RequestParam Long id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false, name = "category") List<Long> categories,
                                    @RequestParam(required = false, name = "volume") List<Long> volumes,
                                    @RequestParam(defaultValue = "0") Integer pageNumber,
                                    @RequestParam(required = false) Integer pageSize,
                                    Model model){

        try{
            productService.softDeleteById(id);
        }
        catch (NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addPageToModel(name, categories, volumes, pageNumber, pageSize, model);

        return ViewNames.PRODUCT_TABLE_FRAGMENT;
    }

    @IsAdmin
    @GetMapping(PRODUCT_REACTIVATE_PATH)
    public String reactivateProductById(@RequestParam Long id,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false, name = "category") List<Long> categories,
                                        @RequestParam(required = false, name = "volume") List<Long> volumes,
                                        @RequestParam(defaultValue = "0") Integer pageNumber,
                                        @RequestParam(required = false) Integer pageSize,
                                        Model model){

        try{
            productService.activateById(id);
        }
        catch (NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addDeletedPageToModel(name, categories, volumes, pageNumber, pageSize, model);

        return ViewNames.PRODUCT_TABLE_FRAGMENT;
    }


    private void addPageToModel(String name,
                                List<Long> categories,
                                List<Long> volumes,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){

        Page<Product> productPage = productService.filterProductPage(name, categories, volumes, pageNumber, pageSize);

        model.addAttribute("productPage", productPage);
        model.addAttribute("nameQuery", name);
        model.addAttribute("categoriesQuery", categories);
        model.addAttribute("volumesQuery", volumes);
    }


    private void addDeletedPageToModel(String name,
                                       List<Long> categories,
                                       List<Long> volumes,
                                       Integer pageNumber,
                                       Integer pageSize,
                                       Model model){

        Page<Product> productPage = productService.filterDeletedProductsPage(name, categories, volumes, pageNumber, pageSize);

        model.addAttribute("productPage", productPage);
        model.addAttribute("nameQuery", name);
        model.addAttribute("categoriesQuery", categories);
        model.addAttribute("volumesQuery", volumes);
        model.addAttribute("deletedQuery", "true");

    }


}
