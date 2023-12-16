package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.ItemProduct;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    /**
     * @param name filter value to compare with the 'name' field of the Product objects for similarity
     * @param categories List of 'id' field values for Category objects to filter by
     * @param volumes List of 'id' field values for Volume objects to filter by
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of Product objects filtered by the supplied params
     */
    Page<Product> filterProductPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize);


    /**
     * @param name filter value to compare with 'name' field of Product objects for similarity
     * @param categories List of 'id' field values for Category objects to filter by
     * @param volumes List of 'id' field values for Volume objects to filter by
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of Product objects filtered by the supplied params, with the 'deleted' field as true
     */
    Page<Product> filterDeletedProductsPage(String name, List<Long> categories, List<Long> volumes, Integer pageNumber, Integer pageSize);


    /**
     * Creates/Updates and persists a Product object to the datasource using data from the 'formToSave' param.
     *
     * @param formToSave form with corresponding data to create or update Product object in the datasource
     * @throws ItemCreationException if datasource constrains are not met
     * @throws NotFoundException if form contains a non-null 'id' field and corresponding Product object is not found
     */
    void saveProduct(ProductForm formToSave) throws ItemCreationException;


    /**
     * @param product product object to persist to the datasource
     * @return the Product object that was persisted to the datasource
     */
    Product saveProduct(Product product);


    /**
     * @param id 'id' field value of the Product object to retrieve from the datasource
     * @return a ProductForm object mapped from the retrieved Product object
     * @throws NotFoundException if item is not found in datasource
     */
    ProductForm getFormById(Long id);


    /**
     * @param itemCode 'itemCode' field value of the product to retrieve from the datasource
     * @return a ItemProduct object mapped from the retrieved Product object
     * @throws NotFoundException if item is not found in datasource
     */
    ItemProduct getItemProductByCode(String itemCode);


    /**
     * @param id 'id' field value of the Product object to retrieve from the datasource
     * @return Product object
     * @throws NotFoundException if item is not found in datasource
     */
    Product getById(Long id);


    /**
     * @param id 'id' field value of the Product object to soft-delete in the datasource
     * @throws NotFoundException if item is not found in datasource
     */
    void softDeleteById(Long id);


    /**
     * @param id 'id' field value of the Product object to un-mark as soft-deleted in the datasource
     * @throws NotFoundException if item is not found in datasource
     */
    void activateById(Long id);




}
