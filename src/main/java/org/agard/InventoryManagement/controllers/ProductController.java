package org.agard.InventoryManagement.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.CategoryService;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class ProductController {

    public static final String PRODUCT_PATH = "/products";

    public static final String PRODUCT_UPDATE_PATH = "/products/update";

    public static final String PRODUCT_DELETE_PATH = "/products/delete";

    public static final String PRODUCT_TABLE_PATH = "/products/table";

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
                                  Model model){

        addPageToModel(nameQuery, categoriesQuery, volumesQuery, pageNumber, pageSize, model);

        return ViewNames.PRODUCT_TABLE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @GetMapping(PRODUCT_UPDATE_PATH)
    public String getUpdateForm(Model model,
                                @RequestParam(required = false) Long id){
        ProductForm productForm;
        if(id == null){
            productForm = new ProductForm();
        }
        else {
            productForm = productService.getFormById(id);
            if(productForm == null){
                throw new NotFoundException( "Product not found for ID: " + id);
            }
        }

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return ViewNames.PRODUCT_UPDATE_FRAGMENT;
    }


    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @PostMapping(PRODUCT_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid ProductForm productForm,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model){

        if(!bindingResult.hasErrors()){
            productService.saveProduct(productForm);
            response.setStatus(201);
            model.addAttribute("productForm", new ProductForm());
        }

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return ViewNames.PRODUCT_UPDATE_FRAGMENT;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(PRODUCT_DELETE_PATH)
    public String deleteProductById(@RequestParam Long id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false, name = "category") List<Long> categories,
                                    @RequestParam(required = false, name = "volume") List<Long> volumes,
                                    @RequestParam(defaultValue = "0") Integer pageNumber,
                                    @RequestParam(required = false) Integer pageSize,
                                    Model model){

        if(productService.deleteById(id)){
            addPageToModel(name, categories, volumes, pageNumber, pageSize, model);
            return ViewNames.PRODUCT_TABLE_FRAGMENT;
        }

        throw new NotFoundException("Product not found for ID: " + id);
    }


    private void addPageToModel(String name,
                                List<Long> categories,
                                List<Long> volumes,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){
        Page<Product> productPage = productService.filterProducts(name, categories, volumes, pageNumber, pageSize);

        model.addAttribute("productPage", productPage);
        model.addAttribute("nameQuery", name);
        model.addAttribute("categoriesQuery", categories);
        model.addAttribute("volumesQuery", volumes);
    }


}
