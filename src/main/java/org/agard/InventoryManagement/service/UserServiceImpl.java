package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.config.UserRole;
import org.agard.InventoryManagement.domain.User;
import org.agard.InventoryManagement.mappers.UserFormMapper;
import org.agard.InventoryManagement.repositories.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, PagingService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserFormMapper userFormMapper;

    private final Sort defaultSort = Sort.by("role")
            .and(Sort.by("lastName"));

    @Override
    public Page<UserForm> filterUserPage(String lastName, UserRole role, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(lastName)){
            lastName = null;
        }

        String roleName = role != null ? role.name : null;

        return userRepository.findAllWithFilters(lastName, roleName, pageRequest)
                .map(userFormMapper::userToUserForm);
    }

    @Override
    public UserForm getFormById(Long id) {
        return userFormMapper.userToUserForm(
                getById(id)
        );
    }


    /**
     * Creates/Updates and persists a User object to the datasource using data from the 'formToSave' param.
     * To create a new User object or update the password field, the 'password' and 'passwordConfirm' fields
     * of the 'formToSave' param must match or an ItemCreationException is thrown. The currently authenticated
     * User cannot update their User Role or an ItemCreationException is thrown.
     *
     * @param formToSave form with corresponding data to create or update a User object in the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     * @throws ItemCreationException if datasource constraints are not met, or if the form has invalid update fields
     */
    @Override
    @Transactional
    public void saveUser(UserForm formToSave, Long currentAdminId) throws ItemCreationException {
        User userToSave;
        boolean isNewUser = false;
        if(formToSave.getId() == null){
            userToSave = new User();
            isNewUser = true;
        }
        else {
            userToSave = getById(formToSave.getId());
        }

        if(formToSave.getPassword() != null && formToSave.getPasswordConfirm() != null){
            if(formToSave.getPassword().equals(formToSave.getPasswordConfirm())){
                userToSave.setPassword(passwordEncoder.encode(formToSave.getPassword()));
            }
            else {
                throw new ItemCreationException("Password and Password Confirm inputs do not match");
            }
        }
        else if(formToSave.getPassword() == null && formToSave.getPasswordConfirm() == null){
            if(isNewUser){
                throw new ItemCreationException("A password must be set for a new user");
            }
        }
        else {
            throw new ItemCreationException("Password and Password Confirm inputs do not match");
        }

        if(!isNewUser && userToSave.getId().equals(currentAdminId)){
            if(!formToSave.getRole().name.equals(userToSave.getRole())){
                throw new IllegalArgumentException("Attempting to change user role of current user");
            }
        }
        userToSave.setRole(formToSave.getRole().name);
        userToSave.setUsername(formToSave.getUsername());
        userToSave.setFirstName(formToSave.getFirstName());
        userToSave.setLastName(formToSave.getLastName());


        try{
            userRepository.save(userToSave);
        }
        catch (RuntimeException e){
            if(e.getCause() instanceof ConstraintViolationException){
                throw new ItemCreationException("Usernames must be unique, this username already exists");
            } else{
                throw e;
            }
        }

    }

    /**
     * Marks the 'enabled' field of the retrieved User object as false.
     * The supplied 'id' should not match that of the currently authenticated user
     * or an IllegalArgumentException is thrown.
     *
     * @param id             'id' field value of the User object to mark as 'disabled' in the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     */
    @Override
    public void disableUserById(Long id, Long currentAdminId) {
        if(id.equals(currentAdminId)){
            throw new IllegalArgumentException("Id values are the same");
        }
        User userToDisable = getById(id);
        userToDisable.setEnabled(false);
        userRepository.save(userToDisable);
    }


    @Override
    public void enableUserById(Long id) {
        User userToEnable = getById(id);
        userToEnable.setEnabled(true);
        userRepository.save(userToEnable);
    }

    /**
     * Deletes a User object from the datasource using the supplied 'id' param.
     * The supplied 'id' should not match that of the currently authenticated user
     * or an IllegalArgumentException is thrown.
     *
     * @param id             'id' field value of the User object to delete from the datasource
     * @param currentAdminId the 'id' field value of the currently authenticated User
     */
    @Override
    public void deleteById(Long id, Long currentAdminId) {
        if(id.equals(currentAdminId)){
            throw new IllegalArgumentException("Id values are the same");
        }
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("User not found for given ID");
    }

    private User getById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    throw new NotFoundException("User not found for given ID");
                });
    }
}
