package org.agard.InventoryManagement.ViewModels;

import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.agard.InventoryManagement.domain.Product;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(of = {"product"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemForm {

    private Long id;

    @NotNull
    private ItemProduct product;

    @NotNull
    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;

    @NotNull
    @Positive(message = "Price must be a positive number")
    private BigDecimal price;

}
