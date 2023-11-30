package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;

import java.util.List;
import java.util.Set;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getById(Long id);

    Set<Product> getProductsForCategory(Category category);

    void saveCategory(Category category);

    void deleteById(Long id);
}
