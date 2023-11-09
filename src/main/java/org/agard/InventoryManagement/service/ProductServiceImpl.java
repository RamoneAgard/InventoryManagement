package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Product;
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

    private final Integer DEFAULT_PAGE_SIZE = 20;

    //Private method to construct page requests from repository based on which page and how big
    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){
        int pageNumberRequest;

        if(pageNumber == null || pageNumber <= 0){
            pageNumberRequest = 0;
        }
        else{
            pageNumberRequest = pageNumber - 1;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > 50)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        Sort defaultSort = Sort.by("category.name").and(Sort.by("volume.valueCode"));

        return PageRequest.of(pageNumberRequest, pageSize, defaultSort);
    }


    /**
     * @param pageNumber page number starting from 1, or null (Default is 1)
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
     * @param pageNumber Page number starting from 1 or null (Default is 1)
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
     * @param toSave the Product to save to database, new or existing
     */
    @Override
    public void saveProduct(Product toSave) {
        productRepository.save(toSave);
    }


    /**
     * @param id ID of the product to retrieve from database
     * @return Product object with argument ID or null
     */
    @Override
    public Product getById(Long id) {
        return productRepository.findById(id).orElse(null);
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
