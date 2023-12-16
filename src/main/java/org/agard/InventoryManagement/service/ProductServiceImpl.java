package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ItemProduct;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.mappers.ProductMapper;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService, PagingService {

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    private final VolumeService volumeService;

    private final ProductMapper productMapper;

    private final Sort defaultSort = Sort.by("category.name")
            .and(Sort.by("volume.valueCode"));


    @Override
    public Page<Product> filterProductPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(name)){
            name = null;
        }
        if(categories != null && categories.size() == 0){
            categories = null;
        }
        if(volumes != null && volumes.size() == 0){
            volumes = null;
        }

        return productRepository.findAllWithFilters(name, categories, volumes, pageRequest);
    }

    @Override
    public Page<Product> filterDeletedProductsPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(name)){
            name = null;
        }
        if(categories != null && categories.size() == 0){
            categories = null;
        }
        if(volumes != null && volumes.size() == 0){
            volumes = null;
        }

        return productRepository.findAllDeletedWithFilters(name, categories, volumes, pageRequest);
    }


    @Override
    @Transactional
    public void saveProduct(@Valid ProductForm formToSave) throws ItemCreationException {
        Product productToSave;
        if(formToSave.getId() == null){
            productToSave = new Product();
        }
        else{
            productToSave = getById(formToSave.getId());
        }
        productToSave.setCategory(categoryService.getById(formToSave.getCategoryId()));
        productToSave.setVolume(volumeService.getById(formToSave.getVolumeId()));
        productToSave.setUpc(formToSave.getUpc());
        productToSave.setItemCode(formToSave.getItemCode());
        productToSave.setName(formToSave.getName());
        productToSave.setCost(formToSave.getCost());
        productToSave.setPrice(formToSave.getPrice());
        productToSave.setUnitSize(formToSave.getUnitSize());
        productToSave.setStock(formToSave.getStock());

        try{
            productRepository.save(productToSave);
        }
        catch (RuntimeException e){
            if(e.getCause() instanceof ConstraintViolationException){
                throw new ItemCreationException("Product upc and item-code must be unique");
            } else{
                throw e;
            }
        }
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }


    @Override
    public ProductForm getFormById(Long id) {
        Product product = getById(id);
        return productMapper.productToProductForm(product);
    }

    @Override
    public ItemProduct getItemProductByCode(String itemCode) {
        Product product = productRepository.findByItemCodeEqualsIgnoreCaseAndDeletedFalse(itemCode);
        if(product == null){
            throw new NotFoundException("Product not found for Item Code: " + itemCode);
        }
        return productMapper.productToItemProduct(product);
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    throw new NotFoundException("Product not found for ID: " + id);
                        });
    }


    @Override
    public void softDeleteById(Long id) {
        if(productRepository.existsById(id)){
            productRepository.softDeleteById(id);
            return;
        }
        throw new NotFoundException("Product not found for ID: " + id);
    }

    @Override
    public void activateById(Long id) {
        if(productRepository.existsById(id)){
            productRepository.reactiveById(id);
            return;
        }
        throw new NotFoundException("Product not found for ID: " + id);
    }
}
