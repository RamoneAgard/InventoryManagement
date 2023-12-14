package org.agard.InventoryManagement.mappers;

import org.agard.InventoryManagement.ViewModels.UserForm;
import org.agard.InventoryManagement.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserFormMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "passwordConfirm", ignore = true)
    UserForm userToUserForm(User user);
}
