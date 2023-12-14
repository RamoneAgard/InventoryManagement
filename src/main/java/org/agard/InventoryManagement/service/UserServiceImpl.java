package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.ItemDeleteException;
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

    @Override
    @Transactional
    public void saveUser(UserForm formToSave, Long currentAdminId) {
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
                throw new ItemCreationException("Cannot change user role while in use");
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
            String message = "Something went wrong saving this User";
            if(e.getCause() instanceof ConstraintViolationException){
                message = "Usernames must be unique, this username already exists";
            }
            throw new ItemCreationException(message);
        }

    }

    @Override
    public void disableUserById(Long id, Long currentAdminId) {
        if(id.equals(currentAdminId)){
            throw new ItemCreationException("Cannot disable user while in use!");
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

    @Override
    public void deleteById(Long id, Long currentAdminId) {
        if(id.equals(currentAdminId)){
            throw new ItemDeleteException("Cannot delete user while in use!");
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
