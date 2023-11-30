package org.agard.InventoryManagement.ViewModels;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemProduct {

    private Long id;

    @NotBlank(message = "Non-blank, 6 character item code required")
    @Size(min = 6, max= 7)
    private String itemCode;

    @Size(min = 2, max = 40)
    @NotBlank(message = "Non-blank product name required")
    private String name;

    @NotBlank(message = "Volume Description cannot be blank")
    @Size(min = 3, max = 15)
    private String volumeDescription;

    @Positive(message = "Valid unit size required")
    @NotNull(message = "Enter a unit size")
    private Integer unitSize;
}
