package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Volume;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VolumeService {

    /**
     * @return a List of all Volume objects from the datasource
     */
    List<Volume> getAllVolumes();


    /**
     * @param description filter value to compare with the 'description' field of Volume objects for similarity
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of Volume objects filtered by the supplied params
     */
    Page<Volume> filterVolumePage(String description, Integer pageNumber, Integer pageSize);


    /**
     * @param description filter value to compare with the 'description' field of Volume objects for similarity
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of Volume objects filtered by the supplied params, with the 'deleted' field as true
     */
    Page<Volume> filterDeletedVolumePage(String description, Integer pageNumber, Integer pageSize);


    /**
     * @param id 'id' field value of the Volume object to retrieve from the datasource
     * @return Volume object
     * @throws NotFoundException if item is not found in the datasource
     */
    Volume getById(Long id);


    /**
     * @param volume Volume object to persist to datasource
     * @throws ItemCreationException if datasource constraints are not met
     * @throws NotFoundException if Category object has non-null 'id' field and the object
     *                           is not found in the datasource
     */
    void saveVolume(Volume volume) throws ItemCreationException;


    /**
     * @param id 'id' field value of the Volume object to soft-delete in the datasource
     * @throws NotFoundException if item is not found in the datasource
     */
    void softDeleteById(Long id);


    /**
     * @param id 'id' field value of the Volume object to un-mark as soft-deleted in the datasource
     * @throws NotFoundException if item is not found in the datasource
     */
    void activateById(Long id);
}
