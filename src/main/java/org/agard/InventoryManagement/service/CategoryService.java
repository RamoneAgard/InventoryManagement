package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    /**
     * @return A List of all Category objects from the datasource
     */
    List<Category> getAllCategories();


    /**
     * @param name  filter value to compare with 'name' field of Category objects for similarity
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of Category objects filtered by the supplied params
     */
    Page<Category> filterCategoryPage(String name, Integer pageNumber, Integer pageSize);


    /**
     * @param name  filter value to compare with 'name' field of Category objects for similarity
     * @param pageNumber  the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize  the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return  a Page of Category objects filtered by the supplied params, with the 'deleted' field as true
     */
    Page<Category> filterDeletedCategoryPage(String name, Integer pageNumber, Integer pageSize);


    /**
     * @param id 'id' field value of the Category object to retrieve from the datasource
     * @return Category object
     * @throws NotFoundException if item is not found in the datasource
     */
    Category getById(Long id);


    /**
     * @param category Category object to persist to datasource
     * @throws ItemCreationException if datasource constraints are not met
     * @throws NotFoundException if Category object has non-null 'id' field and the object
     *                           is not found in the datasource
     */
    void saveCategory(Category category) throws ItemCreationException;


    /**
     * @param id 'id' field value of Category object to soft-delete in the datasource
     * @throws NotFoundException if item is not found in the datasource
     */
    void softDeleteById(Long id);


    /**
     * @param id 'id' field value of Category object to un-mark as soft-deleted in the datasource
     * @throws NotFoundException if item is not found in the datasource
     */
    void activateById(Long id);
}
