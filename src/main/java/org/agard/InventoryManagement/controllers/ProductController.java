package org.agard.InventoryManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.agard.InventoryManagement.service.ProductService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    public static final String PRODUCT_PATH = "/products";
    public static final String PRODUCT_ADD_PATH = "/products/update";

    private final ProductService productService;

    @GetMapping(PRODUCT_PATH)
    public String productList(Model model,
                              @RequestParam(required = false) String name,
                              @RequestParam(defaultValue = "1") Integer pageNumber,
                              @RequestParam(required = false) Integer pageSize){

        Page<Product> productPage = productService.getProductList(name, pageNumber, pageSize);
        List<Product> products = productPage.getContent();

        boolean hasNext = productPage.hasNext();
        boolean hasPrevious = productPage.hasPrevious();

        model.addAttribute("products", products);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        model.addAttribute("nameQuery", name);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("pageSize", pageSize);
        return ViewNames.PRODUCT_VIEW;
    }

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

        model.addAttribute("product", product);

        return ViewNames.PRODUCT_UPDATE;
    }


    @PostMapping(PRODUCT_ADD_PATH)
    public String processCreateOrUpdate(@Valid Product product,
                                        BindingResult bindingResult,
                                        Model model){

        if(bindingResult.hasErrors()){
            return ViewNames.PRODUCT_UPDATE;
        }

        productService.saveProduct(product);

        return "redirect:" + PRODUCT_PATH;
    }



}
