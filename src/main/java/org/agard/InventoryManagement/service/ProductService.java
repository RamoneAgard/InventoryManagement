package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.ItemProduct;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Page<Product> getDefaultProductList(Integer pageNumber, Integer pageSize);

    Page<Product> filterProductPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize);

    void saveProduct(ProductForm formToSave);

    Product saveProduct(Product product);

    ProductForm getFormById(Long id);

    ItemProduct getItemProductByCode(String itemCode);

    Product getById(Long id);

    void deleteById(Long id);




}
