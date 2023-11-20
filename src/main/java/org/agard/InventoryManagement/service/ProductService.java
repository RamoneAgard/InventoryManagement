package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Page<Product> getDefaultProductList(Integer pageNumber, Integer pageSize);

    Page<Product> filterProducts(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize);

    void saveProduct(ProductForm formToSave);

    ProductForm getFormById(Long id);

    boolean deleteById(Long id);




}
