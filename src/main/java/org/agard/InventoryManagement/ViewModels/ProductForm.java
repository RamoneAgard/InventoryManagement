package org.agard.InventoryManagement.ViewModels;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Volume;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductForm {

    private Long id;

    @NotBlank(message = "Non-blank, 12 digit universal product code required ")
    @Size(min = 12, max = 12)
    private String upc;

    @NotBlank(message = "Non-blank, 6 character item code required")
    @Size(min = 6, max= 7)
    private String itemCode;

    @Size(min = 2, max = 40)
    @NotBlank(message = "Non-blank product name required")
    private String name;

    @NotNull
    @Positive
    private Long categoryId;

    @Positive(message = "Valid price value required")
    @NotNull(message = "Enter a product price")
    private BigDecimal price;

    @Positive(message = "Valid cost value required")
    @NotNull(message = "Enter a product cost")
    private BigDecimal cost;

    @Positive(message = "Valid unit size required")
    @NotNull(message = "Enter a unit size")
    private Integer unitSize;

    @NotNull
    @Positive
    private Long volumeId;

    @PositiveOrZero(message = "Enter a non-negative in-stock value")
    @NotNull(message = "Enter an in-stock value")
    private Integer stock;


}
