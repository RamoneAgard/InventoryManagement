package org.agard.InventoryManagement.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ItemProduct;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.mappers.ProductMapper;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    private final VolumeService volumeService;

    private final ProductMapper productMapper;

    private final Integer DEFAULT_PAGE_SIZE = 20;

    private final Integer MAX_PAGE_SIZE = 50;

    //Private method to construct page requests from repository based on which page and how big
    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){

        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > MAX_PAGE_SIZE)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        Sort defaultSort = Sort.by("category.name").and(Sort.by("volume.valueCode"));

        return PageRequest.of(pageNumber, pageSize, defaultSort);
    }


    /**
     * @param pageNumber page number starting from 0, or null (Default is 0)
     * @param pageSize Number of elements per page or null (Default is DEFAULT_PAGE_SIZE defined in class)
     * @return Page of Product elements meeting input parameters
     */
    @Override
    public Page<Product> getDefaultProductList(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        return productRepository.findAll(pageRequest);
    }


    /**
     * @param name       Search name of the product or null
     * @param categories List of category names or null
     * @param volumes    List of volume descriptions or null
     * @param pageNumber Page number starting from 0 or null (Default is 0)
     * @param pageSize   Number of elements per page or null (Default is DEFAULT_PAGE_SIZE defined in class)
     * @return Page of Product elements meeting input parameters
     */
    @Override
    public Page<Product> filterProductPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

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

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

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


    /**
     * @param formToSave the Product to save to database, new or existing
     */
    @Override
    public void saveProduct(@Valid ProductForm formToSave) {
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

        productRepository.save(productToSave);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }


    /**
     * @param id ID of the product to retrieve from database
     * @return ProductForm object with argument ID or Exception is thrown
     */
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


    /**
     * @param id ID of the Product to delete
     */
    @Override
    public void deleteById(Long id) {
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
