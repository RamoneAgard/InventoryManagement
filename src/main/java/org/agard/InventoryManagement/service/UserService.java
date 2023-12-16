package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.config.UserRole;
import org.springframework.data.domain.Page;

public interface UserService{

    /**
     * @param lastName filter value to compare with the 'lastName' field of the User objects for similarity
     * @param role the UserRole of the User objects to retrieve
     * @param pageNumber the page number for the result set, starting at 0, null defaults to 0
     * @param pageSize the size of each page for the result set starting 1, 0 or null defaults to implementing default value
     * @return a Page of UserForm objects mapped from the User objects filtered by the supplied params
     */
    Page<UserForm> filterUserPage(String lastName, UserRole role, Integer pageNumber, Integer pageSize);


    /**
     * @param id 'id' field value of the User object to retrieve from the datasource
     * @return UserForm object mapped from the retrieved User object
     * @throws NotFoundException if item is not found in the datasource
     */
    UserForm getFormById(Long id);


    /**
     * Creates/Updates and persists a User object to the datasource using data from the 'formToSave' param.
     *
     * @param formToSave form with corresponding data to create or update a User object in the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     * @throws ItemCreationException if datasource constraints are not met, or if the form has invalid update fields
     * @throws NotFoundException if item is not found in datasource
     */
    void saveUser(UserForm formToSave, Long currentAdminId) throws ItemCreationException;


    /**
     * @param id 'id' field value of the User object to mark as 'disabled' in the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     * @throws NotFoundException if item is not found in datasource
     */
    void disableUserById(Long id, Long currentAdminId);


    /**
     * @param id 'id' field value of the User object to mark as 'enabled' in the datasource
     * @throws NotFoundException if item is not found in datasource
     */
    void enableUserById(Long id);


    /**
     * @param id 'id' field value of the User object to delete from the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     */
    void deleteById(Long id, Long currentAdminId);

}
