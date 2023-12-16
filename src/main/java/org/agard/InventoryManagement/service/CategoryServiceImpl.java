package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.CategoryRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService, PagingService {

    private final CategoryRepository categoryRepository;

    private final Sort defaultSort = Sort.by("name");


    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByDeletedFalse(defaultSort);
    }

    @Override
    public Page<Category> filterCategoryPage(String name, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(name)){
            name = null;
        }

        return categoryRepository.findAllByFilter(name, pageRequest);
    }

    @Override
    public Page<Category> filterDeletedCategoryPage(String name, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest =  buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(name)){
            name = null;
        }

        return categoryRepository.findAllDeletedByFilter(name, pageRequest);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> {
                    throw new NotFoundException("Category not found for ID: " + id);
                });
    }

    @Override
    @Transactional
    public void saveCategory(Category category) throws ItemCreationException {
        try{
            categoryRepository.save(category);
        }
        catch (RuntimeException e){
            if(e.getCause() instanceof ConstraintViolationException){
                throw new ItemCreationException("Category names must be unique");
            } else{
                throw e;
            }
        }
    }


    @Override
    public void softDeleteById(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.softDeleteById(id);
            return;
        }
        throw new NotFoundException("Category not found for ID: " + id);
    }

    @Override
    public void activateById(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.reactivateById(id);
            return;
        }
        throw new NotFoundException("Category not found for ID: " + id);
    }
}
