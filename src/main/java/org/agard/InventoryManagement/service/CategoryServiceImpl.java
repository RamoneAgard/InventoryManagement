package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final Sort defaultSort = Sort.by("name");

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(defaultSort);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Set<Product> getProductsForCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId()).orElse(null);
        if(existingCategory == null){
            return null;
        }
        return existingCategory.getProducts();
    }

    @Override
    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }


    @Override
    public boolean deleteById(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
