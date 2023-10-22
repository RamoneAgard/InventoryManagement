package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final Integer DEFAULT_PAGE_SIZE = 10;

    //Private method to construct page requests from repository based on which page and how big
    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){
        int pageNumberRequest;

        if(pageNumber == null || pageNumber <= 0){
            pageNumberRequest = 0;
        }
        else{
            pageNumberRequest = pageNumber - 1;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > 40)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return PageRequest.of(pageNumberRequest, pageSize);
    }

    @Override
    public Page<Product> getProductList(String name, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        if(name != null && !name.isBlank()){
            return productRepository.findAllByNameIsLikeIgnoreCase("%" + name + "%", pageRequest);
        }

        return productRepository.findAll(pageRequest);
    }

    @Override
    public void saveProduct(Product toSave) {
        productRepository.save(toSave);
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
