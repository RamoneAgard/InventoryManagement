package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    public static final String PRODUCT_PATH = "/products";

    public static final String PRODUCT_ADD_PATH = "/products/update";

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
        model.addAttribute("product", new Product());

        return ViewNames.PRODUCT_VIEW;
    }

    @RequestMapping(value = PRODUCT_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getProductTable(@RequestParam(required = false) String name,
                                  @RequestParam(required = false, name = "category") List<Long> categories,
                                  @RequestParam(required = false, name = "volume") List<Long> volumes,
                                  @RequestParam(defaultValue = "1") Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize,
                                  Model model){
        System.out.println("name: " + name);
        System.out.println("categories: " + categories);
        System.out.println("volumes: " + volumes);
        System.out.println("pageNumber: " + pageNumber);
        System.out.println("pageSize: " + pageSize);

        addPageToModel(name, categories, volumes, pageNumber, pageSize, model);

        return "fragments/product_table.html :: productTable";
    }

    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @GetMapping(PRODUCT_ADD_PATH)
    public String getUpdateForm(Model model,
                                @RequestParam(required = false) Long id){
        Product product;
        if(id == null){
            product = new Product();
        }
        else {
            product = productService.getById(id);
            if(product == null){
                throw new NotFoundException( "Product not found for ID: " + id);
            }
        }

        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return "fragments/product_update_form :: updateForm";
    }


    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @PostMapping(PRODUCT_ADD_PATH)
    public String processCreateOrUpdate(@Valid Product product,
                                        BindingResult bindingResult,
                                        Model model){

        if(!bindingResult.hasErrors()){
            System.out.println(model);
            productService.saveProduct(product);
            model.addAttribute("product", new Product());
        }
        List<Category> categoryList = categoryService.getAllCategories();
        List<Volume> volumeList = volumeService.getAllVolumes();

        model.addAttribute("categories", categoryList);
        model.addAttribute("volumes", volumeList);

        return "fragments/product_update_form :: updateForm";
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(PRODUCT_DELETE_PATH)
    public String deleteProductById(@RequestParam Long id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false, name = "category") List<Long> categories,
                                    @RequestParam(required = false, name = "volume") List<Long> volumes,
                                    @RequestParam(defaultValue = "1") Integer pageNumber,
                                    @RequestParam(required = false) Integer pageSize,
                                    Model model){

        if(productService.deleteById(id)){
            addPageToModel(name, categories, volumes, pageNumber, pageSize, model);
            return "fragments/product_table.html :: productTable";
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
        List<Product> products = productPage.getContent();

        model.addAttribute("products", products);
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        model.addAttribute("nameQuery", name);
        model.addAttribute("categoriesQuery", categories);
        model.addAttribute("volumesQuery", volumes);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageSize", pageSize);
    }


}
