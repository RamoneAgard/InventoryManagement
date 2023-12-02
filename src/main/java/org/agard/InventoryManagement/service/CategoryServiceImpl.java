package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final Integer DEFAULT_PAGE_SIZE = 20;

    private final Integer MAX_PAGE_SIZE = 50;

    private final Sort defaultSort = Sort.by("name");


    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){

        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > MAX_PAGE_SIZE)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return PageRequest.of(pageNumber, pageSize, defaultSort);
    }


    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByDeletedFalse(defaultSort);
    }

    @Override
    public Page<Category> filterCategoryPage(String name, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if(!StringUtils.hasText(name)){
            name = null;
        }

        return categoryRepository.findAllByFilter(name, pageRequest);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> {
                    throw new NotFoundException("Category not found for ID: " + id);
                });
    }

    @Override
    public Set<Product> getProductsForCategory(Category category) {
        Category existingCategory = categoryRepository.findByIdAndDeletedFalse(category.getId()).orElse(null);
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
    public void deleteById(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.softDeleteById(id);
            return;
        }
        throw new NotFoundException("Category not found for ID: " + id);
    }
}
