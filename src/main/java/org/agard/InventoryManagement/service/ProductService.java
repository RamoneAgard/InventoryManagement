package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    Page<Product> getProductList(String name, String categoryName, Integer pageNumber, Integer pageSize);

    void saveProduct(Product toSave);

    Product getById(Long id);




}
