package org.agard.InventoryManagement.ViewModels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agard.InventoryManagement.config.UserRole;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserForm {

    private Long id;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 15)
    private String username;

    @Size(min = 5, max = 20)
    private String password;

    @Size(min = 5, max = 20)
    private String passwordConfirm;

    @NotNull
    @NotBlank
    private String firstName;

    @NotNull
    @NotBlank
    private String lastName;

    @NotNull
    private UserRole role;

    private boolean enabled;

    private LocalDateTime createdDate;


    public void setPassword(String password){
        this.password = StringUtils.hasText(password) ? password : null;
    }

    public void setPasswordConfirm(String password){
        this.passwordConfirm = StringUtils.hasText(password) ? password : null;
    }

}
