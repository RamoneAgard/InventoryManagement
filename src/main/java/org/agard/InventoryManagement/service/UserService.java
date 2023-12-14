package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.config.UserRole;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserForm> filterUserPage(String lastName, UserRole role, Integer pageNumber, Integer pageSize);

    UserForm getFormById(Long id);

    void saveUser(UserForm formToSave, Long currentAdminId);

    void disableUserById(Long id, Long currentAdminId);

    void enableUserById(Long id);

    void deleteById(Long id, Long currentAdminId);

}
