package org.agard.InventoryManagement.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.mappers.ProductFormMapper;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    private final VolumeService volumeService;

    private final ProductFormMapper productFormMapper;

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
    public Page<Product> filterProducts(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        if(!StringUtils.hasText(name)){
            name = null;
        }

        return productRepository.findAllWithFilters(name, categories, volumes, pageRequest);
    }


    /**
     * @param formToSave the Product to save to database, new or existing
     */
    @Override
    public void saveProduct(@Valid ProductForm formToSave) throws NotFoundException {
        Product productToSave;
        if(formToSave.getId() == null){
            productToSave = new Product();
        }
        else{
            productToSave = productRepository.findById(formToSave.getId()).orElseThrow(()-> {
                throw new NotFoundException("Product not found for ID: " + formToSave.getId());
            });
        }
        productToSave.setCategory(categoryService.getById(formToSave.getCategoryId()).orElseThrow(()-> {
            throw new NotFoundException("Category not found for ID: " + formToSave.getCategoryId());
        }));
        productToSave.setVolume(volumeService.getById(formToSave.getVolumeId()).orElseThrow(()-> {
            throw new NotFoundException("Volume not found for ID: " + formToSave.getVolumeId());
        }));
        productToSave.setUpc(formToSave.getUpc());
        productToSave.setItemCode(formToSave.getItemCode());
        productToSave.setName(formToSave.getName());
        productToSave.setCost(formToSave.getCost());
        productToSave.setPrice(formToSave.getPrice());
        productToSave.setUnitSize(formToSave.getUnitSize());
        productToSave.setStock(formToSave.getStock());

        productRepository.save(productToSave);
    }


    /**
     * @param id ID of the product to retrieve from database
     * @return Product object with argument ID or null
     */
    @Override
    public ProductForm getFormById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if(product != null){
            return productFormMapper.productToProductForm(product);
        }
        return null;
    }


    /**
     * @param id ID of the Product to delete
     * @return boolean indicating the success of deleting the target product
     */
    @Override
    public boolean deleteById(Long id) {
        if(productRepository.existsById(id)){
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
