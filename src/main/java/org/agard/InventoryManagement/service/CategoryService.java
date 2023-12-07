package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CategoryService {

    List<Category> getAllCategories();

    Page<Category> filterCategoryPage(String name, Integer pageNumber, Integer pageSize);

    Page<Category> filterDeletedCategoryPage(String name, Integer pageNumber, Integer pageSize);

    Category getById(Long id);

    Set<Product> getProductsForCategory(Category category);

    void saveCategory(Category category);

    void deleteById(Long id);

    void activateById(Long id);
}
